package sopt.comfit.report.infra.webclient;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.report.exception.AIReportErrorCode;
import sopt.comfit.report.infra.dto.CreateReportAiRequestDto;
import sopt.comfit.report.infra.dto.CreateReportAiResponseDto;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiWebClient {

    private final WebClient webClient;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(name = "openai", fallbackMethod = "createReportFallback")
    @Retry(name = "openai")
    @TimeLimiter(name = "openai")
    public Mono<CreateReportAiResponseDto> createReport(CreateReportAiRequestDto request){
        long startTIme = System.currentTimeMillis();

        return webClient.post()
                .uri("/v1/chat/completions")
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.value() == 401,
                        response -> Mono.error(BaseException.type(AIReportErrorCode.AI_AUTH_FAILED)))
                .onStatus(status -> status.value() == 429,
                        response -> Mono.error(BaseException.type(AIReportErrorCode.AI_RATE_LIMITED)))
                .onStatus(status -> status.value() >= 500,
                        response -> Mono.error(BaseException.type(AIReportErrorCode.AI_SERVER_ERROR)))
                .bodyToMono(CreateReportAiResponseDto.class)
                .doOnSubscribe(subscription ->
                        log.info("OpenAI API 호출 시작"))
                .doOnSuccess(response ->
                        log.info("OpenAI API 호출 완료 - duration: {}ms",
                                System.currentTimeMillis() - startTIme))
                .doOnError(error ->
                        log.error("OpenAI API 호출 실패", error));
    }

    private Mono<CreateReportAiResponseDto> createReportFallback(
            CreateReportAiRequestDto request, Throwable t) {

        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("openai");

        if (cb.getState() == CircuitBreaker.State.OPEN) {
            return Mono.error(BaseException.type(AIReportErrorCode.AI_SERVICE_UNAVAILABLE));
        }

        if (t instanceof BaseException) {
            return Mono.error(t);  // 401, 429 등 그대로
        }

        log.warn("OpenAI fallback 실행 - 원인: {}", t.getMessage());
        return Mono.error(BaseException.type(AIReportErrorCode.AI_SERVICE_UNAVAILABLE));
    }
}
