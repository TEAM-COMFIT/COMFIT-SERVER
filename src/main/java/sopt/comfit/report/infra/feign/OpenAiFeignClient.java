package sopt.comfit.report.infra.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import sopt.comfit.report.infra.dto.CreateReportAiRequestDto;
import sopt.comfit.report.infra.dto.CreateReportAiResponseDto;

@FeignClient(
        name = "openai-client",
        url = "https://api.openai.com",
        configuration = OpenAiFeignClientConfig.class
)
public interface OpenAiFeignClient {

    @PostMapping(
            value = "/v1/chat/completions",
            consumes = "application/json"
    )
    CreateReportAiResponseDto createReport(
            @RequestBody CreateReportAiRequestDto request
    );
}