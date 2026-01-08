package sopt.comfit.report.infra.dto;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public record CreateReportAiRequestDto(
        String model,
        List<Message> messages
) {
    public record Message(
            String role,
            String content
    ) {}

    public static CreateReportAiRequestDto from(String prompt) {
        log.info("AI Request 요청");
        return new CreateReportAiRequestDto(
                "gpt-4o-mini",
                List.of(new Message("user", prompt))
        );
    }
}