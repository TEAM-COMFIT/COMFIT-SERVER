package sopt.comfit.report.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import sopt.comfit.company.dto.response.GetReportCompanyResponseDto;
import sopt.comfit.experience.dto.response.GetReportExperienceResponseDto;
import sopt.comfit.global.dto.PageDto;
import sopt.comfit.report.dto.command.MatchExperienceCommandDto;
import sopt.comfit.report.dto.response.AIReportResponseDto;
import sopt.comfit.report.dto.response.GetReportSummaryResponseDto;
import sopt.comfit.report.infra.dto.PreparedDataDto;
import sopt.comfit.report.infra.prompt.AIReportParallelPromptBuilder;
import sopt.comfit.report.infra.prompt.AIReportPromptBuilder;
import sopt.comfit.report.infra.service.RetryableAiCallerService;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIReportFacade {

    private final AIReportQueryService aiReportQueryService;
    private final AIReportCommandService aiReportCommandService;
    private final RetryableAiCallerService aiCaller;


    public PageDto<GetReportSummaryResponseDto> getReportList(Long userId, Pageable pageable, String keyword){
        return aiReportQueryService.getReportList(userId, pageable, keyword);
    }

    public AIReportResponseDto getReport(Long userId, Long reportId){
        return aiReportQueryService.getReport(userId, reportId);
    }

    public GetReportExperienceResponseDto getReportExperience(Long userId){
        return aiReportQueryService.getReportExperience(userId);
    }

    public GetReportCompanyResponseDto getReportCompany(Long companyId){
        return aiReportQueryService.getReportCompany(companyId);
    }

    public AIReportResponseDto matchExperience(MatchExperienceCommandDto command) {
        PreparedDataDto data = aiReportQueryService.prepareData(command);

        log.info("OpenFeign 호출 시작 - companyId: {}, experienceId: {}",
                data.company().getId(), data.experience().getId());

        String content = aiCaller.callSync(
                AIReportPromptBuilder.build(data.company(), data.experience(),
                        data.jobDescription(), data.issues()));

        return AIReportResponseDto.from(
                aiReportCommandService.parseAndSave(content, data.experience(),
                        data.company(), command.jobDescription()));
    }

    public Mono<AIReportResponseDto> matchExperienceAsync(MatchExperienceCommandDto command) {
        return Mono.fromCallable(() -> aiReportQueryService.prepareData(command))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(data -> aiCaller.callAsync(
                                AIReportPromptBuilder.build(data.company(), data.experience(),
                                        command.jobDescription(), data.issues()))
                        .flatMap(content -> Mono.fromCallable(() ->
                                        aiReportCommandService.parseAndSave(content, data.experience(),
                                                data.company(), command.jobDescription()))
                                .subscribeOn(Schedulers.boundedElastic())))
                .map(AIReportResponseDto::from);
    }

    // WebClient + block
    public AIReportResponseDto matchExperienceWebclient(MatchExperienceCommandDto command) {
        PreparedDataDto data = aiReportQueryService.prepareData(command);

        log.info("WebClient+block 호출 시작 - companyId: {}, experienceId: {}",
                data.company().getId(), data.experience().getId());

        String content = aiCaller.callAsync(
                        AIReportPromptBuilder.build(data.company(), data.experience(),
                                data.jobDescription(), data.issues()))
                .block();

        return AIReportResponseDto.from(
                aiReportCommandService.parseAndSave(content, data.experience(),
                        data.company(), command.jobDescription()));
    }

    public Mono<AIReportResponseDto> matchExperienceParallel(MatchExperienceCommandDto command) {
        return Mono.fromCallable(() -> aiReportQueryService.prepareData(command))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(data -> aiCaller.callTask(
                                AIReportParallelPromptBuilder.buildPerspective(data),
                                "Perspectives", "perspectives")
                        .flatMap(perspectivesJson -> aiCaller.callParallel(data, perspectivesJson))
                        .flatMap(mergedJson -> Mono.fromCallable(() ->
                                        aiReportCommandService.parseAndSave(mergedJson, data.experience(),
                                                data.company(), command.jobDescription()))
                                .subscribeOn(Schedulers.boundedElastic())))
                .map(AIReportResponseDto::from);
    }

    // Virtual Thread 방식
    public AIReportResponseDto matchExperienceVirtualThread(MatchExperienceCommandDto command) {
        PreparedDataDto data = aiReportQueryService.prepareData(command);

        String perspectivesJson = aiCaller.callSyncWithField(
                AIReportParallelPromptBuilder.buildPerspective(data),
                "Perspectives", "perspectives");

        String mergedJson = aiCaller.callParallelWithVirtualThread(data, perspectivesJson);

        return AIReportResponseDto.from(
                aiReportCommandService.parseAndSave(mergedJson, data.experience(),
                        data.company(), command.jobDescription()));
    }
}
