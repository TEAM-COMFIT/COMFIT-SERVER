package sopt.comfit.report.infra.webclient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import sopt.comfit.report.infra.dto.CreateReportAiRequestDto;
import sopt.comfit.report.infra.dto.CreateReportAiResponseDto;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiWebClient {

    private final WebClient webClient;

    public Mono<CreateReportAiResponseDto> createReport(CreateReportAiRequestDto request){
        long startTIme = System.currentTimeMillis();

        return webClient.post()
                .uri("/v1/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(CreateReportAiResponseDto.class)
                .doOnSubscribe(subscription ->
                        log.info("OpenAI API 호출 시작 (WebClient)"))
                .doOnSuccess(response ->
                        log.info("OpenAI API 호출 완료(WebClient) - duration : {} ms",
                                System.currentTimeMillis() - startTIme))
                .doOnError(error ->
                        log.error("OpenAI API 호출 실패 (WebClient)", error));
    }
}
