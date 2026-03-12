package sopt.comfit.report.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.global.logging.ContextAwareExecutor;
import sopt.comfit.report.exception.AIReportErrorCode;
import sopt.comfit.report.infra.dto.CreateReportAiRequestDto;
import sopt.comfit.report.infra.dto.CreateReportAiResponseDto;
import sopt.comfit.report.infra.feign.ResilientOpenAiFeignClient;
import sopt.comfit.report.infra.service.RetryableAiCallerService;
import sopt.comfit.report.infra.webclient.AiWebClient;
import sopt.comfit.report.util.JsonUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("RetryableAiCallerService")
class RetryableAiCallerServiceTest {

    private RetryableAiCallerService service;

    @Mock
    private ResilientOpenAiFeignClient feignClient;

    @Mock
    private AiWebClient aiWebClient;

    @Mock
    private ContextAwareExecutor contextAwareExecutor;

    private ObjectMapper objectMapper;
    private JsonUtils jsonUtils;

    // MAX_RETRY = 2: 최초 1회 + 재시도 2회 = 총 3회 시도
    private static final int TOTAL_ATTEMPTS = 3;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        jsonUtils = new JsonUtils(objectMapper);
        service = new RetryableAiCallerService(
                aiWebClient, feignClient, objectMapper, jsonUtils, contextAwareExecutor);
    }

    // ──────────────────────────────────────────────
    // callSync()
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("callSync()")
    class CallSyncTest {

        @Test
        @DisplayName("유효한 JSON 응답이면 그대로 반환한다")
        void returnsCleanedJsonOnSuccess() {
            // given: Feign 클라이언트가 유효한 JSON 반환
            String rawContent = "```json\n{\"key\": \"value\"}\n```";
            given(feignClient.createReport(any(CreateReportAiRequestDto.class)))
                    .willReturn(makeResponse(rawContent));

            // when
            String result = service.callSync("some prompt");

            // then: clean() 처리 후 순수 JSON 반환
            assertThat(result).isEqualTo("{\"key\": \"value\"}");
        }

        @Test
        @DisplayName("파싱 실패가 반복되면 MAX_RETRY 횟수만큼 재시도 후 예외를 던진다")
        void retriesOnParseFailedAndThrows() {
            // AI가 계속 JSON이 아닌 응답을 반환하는 경우 → 최대 재시도 후 포기
            given(feignClient.createReport(any(CreateReportAiRequestDto.class)))
                    .willReturn(makeResponse("NOT A JSON AT ALL!"));

            assertThatThrownBy(() -> service.callSync("prompt"))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(AIReportErrorCode.AI_RESPONSE_PARSE_FAILED));

            // 최초 1회 + 재시도 2회 = 총 3회 호출 검증
            verify(feignClient, times(TOTAL_ATTEMPTS)).createReport(any());
        }

        @Test
        @DisplayName("재시도 불가 오류(AI_SERVICE_UNAVAILABLE)는 즉시 예외를 던진다")
        void doesNotRetryOnNonRetryableError() {
            // 서킷브레이커 OPEN 상태에서 오는 예외는 재시도 대상이 아님
            given(feignClient.createReport(any(CreateReportAiRequestDto.class)))
                    .willThrow(BaseException.type(AIReportErrorCode.AI_SERVICE_UNAVAILABLE));

            assertThatThrownBy(() -> service.callSync("prompt"))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(AIReportErrorCode.AI_SERVICE_UNAVAILABLE));

            // 재시도 없이 1회만 호출되어야 함
            verify(feignClient, times(1)).createReport(any());
        }
    }

    // ──────────────────────────────────────────────
    // callSyncWithField()
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("callSyncWithField()")
    class CallSyncWithFieldTest {

        @Test
        @DisplayName("required 필드가 있는 JSON이면 성공적으로 반환한다")
        void returnsJsonWhenRequiredFieldPresent() {
            // 병렬 호출에서 각 태스크가 자신의 필수 필드를 포함하는지 검증
            String content = "{\"density\": {\"value\": 0.8}}";
            given(feignClient.createReport(any(CreateReportAiRequestDto.class)))
                    .willReturn(makeResponse(content));

            String result = service.callSyncWithField("prompt", "Density", "density");

            assertThat(result).isEqualTo(content);
        }

        @Test
        @DisplayName("required 필드가 없으면 AI_RESPONSE_REQUIRED_FIELD_OMIT 예외 후 재시도한다")
        void retriesWhenRequiredFieldMissing() {
            // AI가 요청한 필드를 빠뜨린 경우 → 재시도 유도
            String noFieldJson = "{\"other\": \"data\"}";
            given(feignClient.createReport(any(CreateReportAiRequestDto.class)))
                    .willReturn(makeResponse(noFieldJson));

            assertThatThrownBy(() -> service.callSyncWithField("prompt", "Density", "density"))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(AIReportErrorCode.AI_RESPONSE_REQUIRED_FIELD_OMIT));

            verify(feignClient, times(TOTAL_ATTEMPTS)).createReport(any());
        }

        @Test
        @DisplayName("첫 시도 실패 후 재시도에서 성공하면 정상 반환한다")
        void successOnSecondAttempt() {
            // 첫 번째: 필드 누락 응답, 두 번째: 정상 응답
            String failContent = "{\"wrong\": \"field\"}";
            String successContent = "{\"density\": {\"value\": 0.9}}";

            given(feignClient.createReport(any(CreateReportAiRequestDto.class)))
                    .willReturn(makeResponse(failContent))
                    .willReturn(makeResponse(successContent));

            String result = service.callSyncWithField("prompt", "Density", "density");

            assertThat(result).isEqualTo(successContent);
            verify(feignClient, times(2)).createReport(any());
        }
    }

    // ──────────────────────────────────────────────
    // 헬퍼
    // ──────────────────────────────────────────────
    private CreateReportAiResponseDto makeResponse(String content) {
        return new CreateReportAiResponseDto(
                List.of(new CreateReportAiResponseDto.Choice(
                        new CreateReportAiResponseDto.Message("assistant", content)
                ))
        );
    }
}
