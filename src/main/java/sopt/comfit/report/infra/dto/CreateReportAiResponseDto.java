package sopt.comfit.report.infra.dto;

import java.util.List;

public record CreateReportAiResponseDto(
        List<Choice> choices
) {
    public record Choice(Message message) {}
    public record Message(String role, String content) {}

    public String getContent() {
        if (choices == null || choices.isEmpty()) {
            throw new IllegalStateException("OpenAI response has no choices");
        }
        return choices.getFirst().message().content();
    }

}