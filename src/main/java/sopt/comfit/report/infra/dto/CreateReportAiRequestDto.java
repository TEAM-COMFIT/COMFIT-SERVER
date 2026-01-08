package sopt.comfit.report.infra.dto;

import java.util.List;

public record CreateReportAiRequestDto(
        String model,
        List<Message> messages
) {
    public record Message(
            String role,
            String content
    ) {}

    public static CreateReportAiRequestDto from(String prompt) {
        return new CreateReportAiRequestDto(
                "gpt-4o",
                List.of(new Message("user", prompt))
        );
    }
}