package sopt.comfit.report.infra;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.report.exception.AIReportErrorCode;
import sopt.comfit.report.infra.dto.CreateReportAiResponseDto;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CreateReportAiResponseDto")
class CreateReportAiResponseDtoTest {

    @Test
    @DisplayName("정상 응답에서 content를 반환한다")
    void getContentReturnsMessageContent() {
        // OpenAI 응답의 choices[0].message.content 추출 검증
        var response = new CreateReportAiResponseDto(
                List.of(new CreateReportAiResponseDto.Choice(
                        new CreateReportAiResponseDto.Message("assistant", "{\"key\":\"value\"}")
                ))
        );

        assertThat(response.getContent()).isEqualTo("{\"key\":\"value\"}");
    }

    @Test
    @DisplayName("choices가 null이면 AI_RESPONSE_EMPTY 예외를 던진다")
    void throwsEmptyExceptionWhenChoicesNull() {
        // OpenAI가 빈 응답을 반환하는 엣지 케이스
        var response = new CreateReportAiResponseDto(null);

        assertThatThrownBy(response::getContent)
                .isInstanceOf(BaseException.class)
                .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                        .isEqualTo(AIReportErrorCode.AI_RESPONSE_EMPTY));
    }

    @Test
    @DisplayName("choices가 빈 리스트이면 AI_RESPONSE_EMPTY 예외를 던진다")
    void throwsEmptyExceptionWhenChoicesEmpty() {
        var response = new CreateReportAiResponseDto(Collections.emptyList());

        assertThatThrownBy(response::getContent)
                .isInstanceOf(BaseException.class)
                .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                        .isEqualTo(AIReportErrorCode.AI_RESPONSE_EMPTY));
    }
}
