package sopt.comfit.report.infra;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import sopt.comfit.report.infra.dto.CreateReportAiRequestDto;
import sopt.comfit.report.infra.dto.CreateReportAiResponseDto;

@FeignClient(
        name = "openai-client",
        url = "https://api.openai.com",
        configuration = OpenAiClientConfig.class
)
public interface OpenAiClient {

    @PostMapping(
            value = "/v1/chat/completions",
            consumes = "application/json"
    )
    CreateReportAiResponseDto createReport(
            @RequestBody CreateReportAiRequestDto request
    );
}