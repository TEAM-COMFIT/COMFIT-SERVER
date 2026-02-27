package sopt.comfit.report.infra.feign;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.report.exception.AIReportErrorCode;
import sopt.comfit.report.infra.dto.CreateReportAiRequestDto;
import sopt.comfit.report.infra.dto.CreateReportAiResponseDto;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResilientOpenAiFeignClient {

    private final OpenAiFeignClient openAiFeignClient;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @RateLimiter(name = "openai")
    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(
            name = "openai-feign", fallbackMethod = "createReportFallback")
    @Retry(name = "openai-feign")
    public CreateReportAiResponseDto createReport(CreateReportAiRequestDto request) {
        long startTime = System.currentTimeMillis();
        log.info("OpenAI API 호출 시작 (OpenFeign)");

        CreateReportAiResponseDto response = openAiFeignClient.createReport(request);

        log.info("OpenAI API 호출 완료 (OpenFeign) - durationL {}ms",
                System.currentTimeMillis() - startTime);

        return response;
    }

    private CreateReportAiResponseDto createReportFallback(CreateReportAiRequestDto request, Throwable t) {

        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("openai-feign");

        if(cb.getState() == CircuitBreaker.State.OPEN){
            log.warn("서킷브레이커 OPEN 상태 - 요청 차단");
            throw BaseException.type(AIReportErrorCode.AI_SERVICE_UNAVAILABLE);
        }

        if (t instanceof BaseException) {
            throw (BaseException) t;
        }

        log.warn("OpenAI fallback 실행 (OpenFeign) - 원인{}", t.getMessage());
        throw BaseException.type(AIReportErrorCode.AI_SERVICE_UNAVAILABLE);
    }
}
