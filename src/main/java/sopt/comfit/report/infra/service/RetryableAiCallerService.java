package sopt.comfit.report.infra.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.global.logging.ContextAwareExecutor;
import sopt.comfit.report.exception.AIReportErrorCode;
import sopt.comfit.report.infra.dto.CreateReportAiRequestDto;
import sopt.comfit.report.infra.dto.PreparedDataDto;
import sopt.comfit.report.infra.feign.OpenAiFeignClient;
import sopt.comfit.report.infra.feign.ResilientOpenAiFeignClient;
import sopt.comfit.report.infra.prompt.AIReportParallelPromptBuilder;
import sopt.comfit.report.infra.webclient.AiWebClient;
import sopt.comfit.report.util.JsonUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryableAiCallerService {

    private final AiWebClient aiWebClient;
    private final ResilientOpenAiFeignClient openAiFeignClient;
    private final ObjectMapper objectMapper;
    private final JsonUtils jsonUtils;
    private static final int MAX_RETRY = 2;
    private final ContextAwareExecutor contextAwareExecutor;

    // Feign 동기 호출
    public String callSync(String prompt) {
        for (int attempt = 0; attempt <= MAX_RETRY; attempt++) {
            try {
                String content = openAiFeignClient
                        .createReport(CreateReportAiRequestDto.from(prompt))
                        .getContent();

                String cleaned = jsonUtils.clean(content);

                validateJsonSync(cleaned);

                return cleaned;
            } catch (BaseException e) {
                if (isRetryableError(e) && attempt < MAX_RETRY) {
                    log.warn("파싱 실패, 재시도 {}", attempt + 1);
                    continue;
                }
                throw e;
            }
        }
        throw BaseException.type(AIReportErrorCode.AI_RESPONSE_PARSE_FAILED);
    }

    // WebFlux 단일 호출
    public Mono<String> callAsync(String prompt) {
        return Mono.defer(() -> aiWebClient.createReport(CreateReportAiRequestDto.from(prompt))
                        .map(response -> jsonUtils.clean(response.getContent()))
                        .flatMap(this::validateJson))
                .retryWhen(retrySpec("Async"));
    }

    // 병렬 호출
    public Mono<String> callParallel(PreparedDataDto data, String perspectivesJson) {
        Mono<String> densityMono = callTask(
                AIReportParallelPromptBuilder.buildDensity(data, perspectivesJson),
                "Density", "density");

        Mono<String> appealPointMono = callTask(
                AIReportParallelPromptBuilder.buildAppealPoint(data, perspectivesJson),
                "AppealPoint", "appealPoint");

        Mono<String> suggestionMono = callTask(
                AIReportParallelPromptBuilder.buildSuggestion(data, perspectivesJson),
                "Suggestion", "suggestion");

        Mono<String> guidanceMono = callTask(
                AIReportParallelPromptBuilder.buildGuidance(data, perspectivesJson),
                "Guidance", "guidance");

        return Mono.zip(densityMono, appealPointMono, suggestionMono, guidanceMono)
                .map(tuple -> jsonUtils.merge(perspectivesJson, tuple.getT1(),
                        tuple.getT2(), tuple.getT3(), tuple.getT4()))
                .doOnNext(r -> log.info("4개 병렬 호출 및 머지 완료"));
    }

    // 호출 + 검증
    public Mono<String> callTask(String prompt, String taskName, String requiredField) {
        return aiWebClient.createReport(CreateReportAiRequestDto.from(prompt))
                .map(response -> jsonUtils.clean(response.getContent()))
                .flatMap(content -> validateJsonWithField(content, requiredField))
                .retryWhen(retrySpec(taskName))
                .doOnSubscribe(s -> log.info("{} 호출 시작", taskName))
                .doOnNext(r -> log.info("{} 호출 완료", taskName));
    }

    // 동기 호출
    public String callSyncWithField(String prompt, String taskName, String requiredField) {
        for (int attempt = 0; attempt <= MAX_RETRY; attempt++) {
            try {
                log.info("{} 호출 시작", taskName);

                String content = openAiFeignClient
                        .createReport(CreateReportAiRequestDto.from(prompt))
                        .getContent();
                String cleaned = jsonUtils.clean(content);
                validateJsonSyncWithField(cleaned, requiredField);

                log.info("{} 호출 완료", taskName);
                return cleaned;
            } catch (BaseException e) {
                if (attempt < MAX_RETRY && isRetryableError(e)) {
                    log.warn("{} 재시도 {}", taskName, attempt + 1);
                    continue;
                }
                throw e;
            }
        }
        throw BaseException.type(AIReportErrorCode.AI_RESPONSE_PARSE_FAILED);
    }

    // 동기 병렬 호출
    public String callParallelWithVirtualThread(PreparedDataDto data, String perspectivesJson) {
        try (ExecutorService executor = contextAwareExecutor.newVirtualThreadExecutor()) {
            Future<String> densityFuture = contextAwareExecutor.submit(executor, () ->
                    callSyncWithField(
                            AIReportParallelPromptBuilder.buildDensity(data, perspectivesJson),
                            "Density", "density"));  // callSyncWithField로 변경

            Future<String> appealPointFuture = contextAwareExecutor.submit(executor, () ->
                    callSyncWithField(
                            AIReportParallelPromptBuilder.buildAppealPoint(data, perspectivesJson),
                            "AppealPoint", "appealPoint"));

            Future<String> suggestionFuture = contextAwareExecutor.submit(executor, () ->
                    callSyncWithField(
                            AIReportParallelPromptBuilder.buildSuggestion(data, perspectivesJson),
                            "Suggestion", "suggestion"));

            Future<String> guidanceFuture = contextAwareExecutor.submit(executor, () ->
                    callSyncWithField(
                            AIReportParallelPromptBuilder.buildGuidance(data, perspectivesJson),
                            "Guidance", "guidance"));

            return jsonUtils.merge(
                    perspectivesJson,
                    densityFuture.get(),
                    appealPointFuture.get(),
                    suggestionFuture.get(),
                    guidanceFuture.get()
            );
        } catch (Exception e) {
            log.error("AI 병렬 호출 실패 - cause={}", e.getCause().getMessage(), e);
            throw BaseException.type(AIReportErrorCode.AI_CALL_FAILED);
        }
    }


    // 비동기 단일호출 validate
    private Mono<String> validateJson(String content) {
        try {
            objectMapper.readTree(content);
            return Mono.just(content);
        } catch (JsonProcessingException e) {
            return Mono.error(BaseException.type(AIReportErrorCode.AI_RESPONSE_PARSE_FAILED));
        }
    }

    // 비동기 병렬호출 validate
    private Mono<String> validateJsonWithField(String content, String requiredField) {
        try {
            JsonNode node = objectMapper.readTree(content);
            if (!node.has(requiredField)) {
                return Mono.error(BaseException.type(AIReportErrorCode.AI_RESPONSE_REQUIRED_FIELD_OMIT));
            }
            return Mono.just(content);
        } catch (JsonProcessingException e) {
            return Mono.error(BaseException.type(AIReportErrorCode.AI_RESPONSE_PARSE_FAILED));
        }
    }

    // 동기 단일호출 validate
    private void validateJsonSync(String content) {
        try {
            objectMapper.readTree(content);
        } catch (JsonProcessingException e) {
            throw BaseException.type(AIReportErrorCode.AI_RESPONSE_PARSE_FAILED);
        }
    }

    // 동기 병렬호출 validate
    private void validateJsonSyncWithField(String content, String requiredField) {
        try {
            JsonNode node = objectMapper.readTree(content);
            if (!node.has(requiredField)) {
                throw BaseException.type(AIReportErrorCode.AI_RESPONSE_REQUIRED_FIELD_OMIT);
            }
        } catch (JsonProcessingException e) {
            throw BaseException.type(AIReportErrorCode.AI_RESPONSE_PARSE_FAILED);
        }
    }

    // 비동기 재시도 로직
    private Retry retrySpec(String taskName) {
        return Retry.max(MAX_RETRY)
                .filter(e -> e instanceof BaseException &&
                        (((BaseException) e).getErrorCode() == AIReportErrorCode.AI_RESPONSE_PARSE_FAILED ||
                                ((BaseException) e).getErrorCode() == AIReportErrorCode.AI_RESPONSE_REQUIRED_FIELD_OMIT))
                .doBeforeRetry(signal ->
                        log.warn("{} 재시도 {}", taskName, signal.totalRetries() + 1));
    }

    // 동기 재시도 로직
    private boolean isRetryableError(Throwable e) {
        if (!(e instanceof BaseException)) return false;

        AIReportErrorCode code = (AIReportErrorCode) ((BaseException) e).getErrorCode();
        return code == AIReportErrorCode.AI_RESPONSE_PARSE_FAILED ||
                code == AIReportErrorCode.AI_RESPONSE_REQUIRED_FIELD_OMIT;
    }
}

