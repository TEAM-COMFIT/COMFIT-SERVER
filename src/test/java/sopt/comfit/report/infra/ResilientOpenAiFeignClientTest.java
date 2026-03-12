package sopt.comfit.report.infra;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.report.exception.AIReportErrorCode;
import sopt.comfit.report.infra.dto.CreateReportAiRequestDto;
import sopt.comfit.report.infra.dto.CreateReportAiResponseDto;
import sopt.comfit.report.infra.feign.OpenAiFeignClient;
import sopt.comfit.report.infra.feign.ResilientOpenAiFeignClient;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResilientOpenAiFeignClient - fallback 동작")
class ResilientOpenAiFeignClientTest {

    // Resilience4j 어노테이션(@CircuitBreaker, @Retry 등)은 Spring AOP 기반이므로
    // 단위 테스트에서는 fallback 메서드 자체의 동작을 검증
    // (통합 테스트에서는 실제 AOP 경로 검증 필요)

    private ResilientOpenAiFeignClient client;

    @Mock
    private OpenAiFeignClient openAiFeignClient;

    @Mock
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Mock
    private CircuitBreaker circuitBreaker;

    @BeforeEach
    void setUp() {
        client = new ResilientOpenAiFeignClient(openAiFeignClient, circuitBreakerRegistry);
    }

    @Nested
    @DisplayName("createReportFallback()")
    class FallbackTest {

        private final CreateReportAiRequestDto request = CreateReportAiRequestDto.from("test");

        // fallback을 직접 호출하는 헬퍼 (리플렉션)
        private void invokeFallback(Throwable cause) throws Exception {
            Method fallback = ResilientOpenAiFeignClient.class
                    .getDeclaredMethod("createReportFallback",
                            CreateReportAiRequestDto.class, Throwable.class);
            fallback.setAccessible(true);
            fallback.invoke(client, request, cause);
        }

        @Test
        @DisplayName("서킷브레이커가 OPEN 상태이면 AI_SERVICE_UNAVAILABLE 예외를 던진다")
        void throwsServiceUnavailableWhenCircuitOpen() throws Exception {
            // OPEN 상태: 외부 AI 서비스 장애로 차단된 상태
            given(circuitBreakerRegistry.circuitBreaker("openai-feign")).willReturn(circuitBreaker);
            given(circuitBreaker.getState()).willReturn(CircuitBreaker.State.OPEN);

            assertThatThrownBy(() -> invokeFallback(new RuntimeException("timeout")))
                    .cause()
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(AIReportErrorCode.AI_SERVICE_UNAVAILABLE));
        }

        @Test
        @DisplayName("원인이 BaseException이면 그대로 재던진다 (에러 코드 보존)")
        void rethrowsBaseExceptionAsIs() throws Exception {
            // 예: AI_RATE_LIMITED(429) → fallback에서 변환 없이 전파
            given(circuitBreakerRegistry.circuitBreaker("openai-feign")).willReturn(circuitBreaker);
            given(circuitBreaker.getState()).willReturn(CircuitBreaker.State.CLOSED);

            BaseException original = BaseException.type(AIReportErrorCode.AI_RATE_LIMITED);

            assertThatThrownBy(() -> invokeFallback(original))
                    .cause()
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(AIReportErrorCode.AI_RATE_LIMITED));
        }

        @Test
        @DisplayName("서킷브레이커 CLOSED + 일반 예외이면 AI_SERVICE_UNAVAILABLE 예외를 던진다")
        void throwsServiceUnavailableForGenericException() throws Exception {
            // 서킷 CLOSED 상태에서도 알 수 없는 오류는 AI_SERVICE_UNAVAILABLE로 추상화
            given(circuitBreakerRegistry.circuitBreaker("openai-feign")).willReturn(circuitBreaker);
            given(circuitBreaker.getState()).willReturn(CircuitBreaker.State.CLOSED);

            assertThatThrownBy(() -> invokeFallback(new RuntimeException("unknown error")))
                    .cause()
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(AIReportErrorCode.AI_SERVICE_UNAVAILABLE));
        }
    }

    @Nested
    @DisplayName("createReport() - 정상 흐름")
    class NormalFlowTest {

        @Test
        @DisplayName("OpenAI 응답이 정상이면 그대로 반환한다")
        void returnsResponseOnSuccess() {
            // given: Feign 클라이언트가 정상 응답 반환 (Resilience4j AOP 없이 직접 호출)
            CreateReportAiResponseDto mockResponse = new CreateReportAiResponseDto(
                    List.of(new CreateReportAiResponseDto.Choice(
                            new CreateReportAiResponseDto.Message("assistant", "{}")
                    ))
            );
            given(openAiFeignClient.createReport(any())).willReturn(mockResponse);

            CreateReportAiResponseDto result = client.createReport(CreateReportAiRequestDto.from("test"));

            assertThat(result.getContent()).isEqualTo("{}");
        }
    }
}
