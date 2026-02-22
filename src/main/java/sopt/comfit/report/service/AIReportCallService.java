package sopt.comfit.report.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;
import sopt.comfit.company.domain.Company;
import sopt.comfit.company.domain.CompanyIssue;
import sopt.comfit.company.domain.CompanyIssueRepository;
import sopt.comfit.company.domain.CompanyRepository;
import sopt.comfit.company.exception.CompanyErrorCode;
import sopt.comfit.experience.domain.Experience;
import sopt.comfit.experience.domain.ExperienceRepository;
import sopt.comfit.experience.exception.ExperienceErrorCode;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.global.util.JsonCleanUtils;
import sopt.comfit.report.domain.AIReport;
import sopt.comfit.report.domain.AIReportRepository;
import sopt.comfit.report.dto.command.MatchExperienceCommandDto;
import sopt.comfit.report.dto.response.AIReportResponseDto;
import sopt.comfit.report.exception.AIReportErrorCode;
import sopt.comfit.report.infra.dto.CreateReportAiRequestDto;
import sopt.comfit.report.infra.dto.CreateReportAiResponseDto;
import sopt.comfit.report.infra.dto.PreparedDataDto;
import sopt.comfit.report.infra.feign.OpenAiFeignClient;
import sopt.comfit.report.infra.prompt.AIReportParallelPromptBuilder;
import sopt.comfit.report.infra.prompt.AIReportPromptBuilder;
import sopt.comfit.report.infra.webclient.AiWebClient;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIReportCallService {

    private final AiWebClient aiWebClient;
    private final CompanyRepository companyRepository;
    private final ExperienceRepository experienceRepository;
    private final CompanyIssueRepository companyIssueRepository;
    private final ObjectMapper objectMapper;
    private final OpenAiFeignClient openAiFeignClient;
    private final AIReportCommandService aiReportCommandService;


    // OpenFeign 방식
    public AIReportResponseDto matchExperience(MatchExperienceCommandDto command) {
        PreparedDataDto dataDto = prepareData(command);
        log.info("OpenAI API 호출 시작 - companyId: {}, experienceId: {}", dataDto.company().getId(), dataDto.experience().getId());
        long startTime = System.currentTimeMillis();

        CreateReportAiResponseDto response = openAiFeignClient
                .createReport(CreateReportAiRequestDto
                        .from(AIReportPromptBuilder
                                .build(dataDto.company(), dataDto.experience(), dataDto.jobDescription(), dataDto.issues())));

        long duration = System.currentTimeMillis() - startTime;
        log.info("OpenAI API 호출 완료 - companyId: {}, experienceId: {}, duration: {}ms, responseLength: {}",
                dataDto.company().getId(), dataDto.experience().getId(), duration, response.getContent().length());


        AIReport aiReport = aiReportCommandService
                .parseAndSave(response.getContent(), dataDto.experience(), dataDto.company(), command.jobDescription());

        log.info("AI 분석 완료 - userId: {}, reportId: {}, companyId: {}, experienceId: {}",
                command.userId(), aiReport.getId(), dataDto.company().getId(), dataDto.experience().getId());

        return AIReportResponseDto.from(aiReport);
    }

    // WebFlux 사용 비동기
    public Mono<AIReportResponseDto> matchExperienceAsync(MatchExperienceCommandDto command){
        return Mono.fromCallable(() -> prepareData(command))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(data -> aiWebClient
                        .createReport(CreateReportAiRequestDto
                                .from(AIReportPromptBuilder
                                        .build(data.company(), data.experience(),
                                                command.jobDescription(), data.issues())))
                        .flatMap(response ->
                                Mono.fromCallable(() ->
                                                aiReportCommandService.parseAndSave(
                                                        response.getContent(),
                                                        data.experience(),
                                                        data.company(),
                                                        command.jobDescription()
                                                )
                                        )
                                        .subscribeOn(Schedulers.boundedElastic())
                        )
                        .map(AIReportResponseDto::from));
    }

    // WebClient 사용 + block 블로킹 방식
    public AIReportResponseDto matchExperienceWebclient(MatchExperienceCommandDto command) {

        PreparedDataDto dataDto = prepareData(command);

        log.info("WebClient API 호출 시작 - companyId: {}, experienceId: {}", dataDto.company().getId(), dataDto.experience().getId());
        long startTime = System.currentTimeMillis();

        CreateReportAiResponseDto response = aiWebClient
                .createReport(CreateReportAiRequestDto
                        .from(AIReportPromptBuilder
                                .build(dataDto.company(), dataDto.experience(), dataDto.jobDescription(), dataDto.issues())))
                .block();  // 여기서 동기로 대기

        long duration = System.currentTimeMillis() - startTime;
        log.info("WebClient API 호출 완료 - duration: {}ms", duration);

        AIReport aiReport = aiReportCommandService
                .parseAndSave(response.getContent(), dataDto.experience(), dataDto.company(), command.jobDescription());

        return AIReportResponseDto.from(aiReport);
    }

    public Mono<AIReportResponseDto> matchExperienceWebfluxParallel(MatchExperienceCommandDto command) {
        return Mono.fromCallable(() -> prepareData(command))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(data -> callPerspectives(data)
                        .flatMap(perspectivesJson -> callParallelAndMerge(data, perspectivesJson))
                        .flatMap(mergedJson ->
                                Mono.fromCallable(() ->
                                                aiReportCommandService.parseAndSave(
                                                        mergedJson,
                                                        data.experience(),
                                                        data.company(),
                                                        command.jobDescription()
                                                )
                                        )
                                        .subscribeOn(Schedulers.boundedElastic())
                        )
                        .map(AIReportResponseDto::from)
                );
    }

    // ============ 데이터 준비 ============

    private PreparedDataDto prepareData(MatchExperienceCommandDto command) {
        Company company = companyRepository.findById(command.companyId())
                .orElseThrow(() -> BaseException.type(CompanyErrorCode.COMPANY_NOT_FOUND));

        Experience experience = experienceRepository.findByIdAndUserId(command.experienceId(), command.userId())
                .orElseThrow(() -> new BaseException(ExperienceErrorCode.NOT_FOUND_EXPERIENCE));

        List<CompanyIssue> issues = companyIssueRepository.findByCompanyId(command.companyId());

        return PreparedDataDto.of(company, experience, command.jobDescription(), issues);
    }

    // Perspectives 호출

    private Mono<String> callPerspectives(PreparedDataDto data) {
        String prompt = AIReportParallelPromptBuilder.buildPerspective(data);

        return aiWebClient.createReport(CreateReportAiRequestDto.from(prompt))
                .map(CreateReportAiResponseDto::getContent)
                .doOnSubscribe(s -> log.info("Perspectives 호출 시작"))
                .doOnNext(response -> log.info("Perspectives 호출 완료"));
    }

    // 4개 병렬 호출

    private Mono<String> callParallelAndMerge(PreparedDataDto data, String perspectivesJson) {
        Mono<String> densityMono = callTask(
                AIReportParallelPromptBuilder.buildDensity(data, perspectivesJson), "Density");

        Mono<String> appealPointMono = callTask(
                AIReportParallelPromptBuilder.buildAppealPoint(data, perspectivesJson), "AppealPoint");

        Mono<String> suggestionMono = callTask(
                AIReportParallelPromptBuilder.buildSuggestion(data, perspectivesJson), "Suggestion");

        Mono<String> guidanceMono = callTask(
                AIReportParallelPromptBuilder.buildGuidance(data, perspectivesJson), "Guidance");

        return Mono.zip(densityMono, appealPointMono, suggestionMono, guidanceMono)
                .map(tuple ->
                        mergeJson(perspectivesJson, tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4()))
                .doOnNext(r -> log.info("4개 병렬 호출 및 머지 완료"))
                .onErrorMap(e -> {
                    log.error("병렬 호출 실패: {}", e.getMessage());
                    return BaseException.type(AIReportErrorCode.AI_CALL_FAILED);
                });
    }

    private Mono<String> callTask(String prompt, String taskName) {
        return aiWebClient.createReport(CreateReportAiRequestDto.from(prompt))
                .map(CreateReportAiResponseDto::getContent)
                .doOnSubscribe(s -> log.info("{} 호출 시작", taskName))
                .doOnNext(r -> log.info("{} 호출 완료", taskName))
                .doOnError(e -> log.error("{} 실패: {}", taskName, e.getMessage()));
    }



    // 결과 조합 및 저장

    private String mergeJson(String perspectives, String density, String appealPoint, String suggestion, String guidance) {
        try {
            JsonNode perspectivesNode = objectMapper.readTree(perspectives);
            JsonNode densityNode = objectMapper.readTree(density);
            JsonNode appealPointNode = objectMapper.readTree(appealPoint);
            JsonNode suggestionNode = objectMapper.readTree(suggestion);
            JsonNode guidanceNode = objectMapper.readTree(guidance);

            if (perspectivesNode.get("perspectives") == null ||
                    densityNode.get("density") == null ||
                    appealPointNode.get("appealPoint") == null ||
                    suggestionNode.get("suggestion") == null ||
                    guidanceNode.get("guidance") == null) {
                log.error("JSON 필드 누락 - perspectives: {}, density: {}, appealPoint: {}, suggestion: {}, guidance: {}",
                        perspectives, density, appealPoint, suggestion, guidance);
                throw BaseException.type(AIReportErrorCode.AI_RESPONSE_PARSE_FAILED);
            }

            ObjectNode merged = objectMapper.createObjectNode();
            merged.set("perspectives", perspectivesNode.get("perspectives"));
            merged.set("density", densityNode.get("density"));
            merged.set("appealPoint", appealPointNode.get("appealPoint"));
            merged.set("suggestion", suggestionNode.get("suggestion"));
            merged.set("guidance", guidanceNode.get("guidance"));

            return objectMapper.writeValueAsString(merged);
        } catch (JsonProcessingException e) {
            log.error("JSON 머지 실패: {}", e.getMessage());
            throw BaseException.type(AIReportErrorCode.AI_RESPONSE_PARSE_FAILED);
        }
    }
}