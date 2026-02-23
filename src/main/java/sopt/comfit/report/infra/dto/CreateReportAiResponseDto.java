package sopt.comfit.report.infra.dto;

import sopt.comfit.global.exception.BaseException;
import sopt.comfit.report.exception.AIReportErrorCode;

import java.util.List;

public record CreateReportAiResponseDto(
        List<Choice> choices
) {
    public record Choice(Message message) {}
    public record Message(String role, String content) {}

    public String getContent() {
        if (choices == null || choices.isEmpty()) {
            throw BaseException.type(AIReportErrorCode.AI_RESPONSE_EMPTY);
        }
        return choices.getFirst().message().content();
    }

}