package sopt.comfit.report.job;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import sopt.comfit.global.constants.Constants;
import sopt.comfit.report.domain.AIReportJob;
import sopt.comfit.report.dto.command.MatchExperienceCommandDto;
import sopt.comfit.report.infra.dto.PreparedDataDto;
import sopt.comfit.report.infra.prompt.AIReportParallelPromptBuilder;
import sopt.comfit.report.infra.service.RetryableAiCallerService;
import sopt.comfit.report.service.AIReportCommandService;
import sopt.comfit.report.service.AIReportQueryService;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class AIReportJobWorker {

    private final StringRedisTemplate redisTemplate;
    private final AIReportJobService reportJobService;
    private final AIReportQueryService aiReportQueryService;
    private final AIReportCommandService aiReportCommandService;
    private final RetryableAiCallerService aiCaller;

    private Thread worker;

    @PostConstruct
    public void startWorker() {
        worker = Thread.ofVirtual().name("report-job-worker").start(this::listen);
        log.info("ReportJobWorker 시작");
    }

    @PreDestroy
    public void stopWorker() {
        worker.interrupt();
        log.info("ReportJobWorker 종료");
    }

    private void listen() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String jobId = redisTemplate.opsForList()
                        .rightPop(Constants.JOB_QUEUE_KEY, Duration.ofSeconds(30));

                if (jobId == null) continue;

                processJob(Long.parseLong(jobId));
            } catch (Exception e) {
                log.error("Worker 루프 에러", e);
            }
        }
    }

    private void processJob(Long jobId) {
        reportJobService.startProcessing(jobId);

        try {
            MatchExperienceCommandDto command = buildCommand(jobId);
            PreparedDataDto data = aiReportQueryService.prepareData(command);

            String perspectivesJson = aiCaller.callSyncWithField(
                    AIReportParallelPromptBuilder.buildPerspective(data),
                    "Perspectives", "perspectives");

            String mergedJson = aiCaller.callParallelWithVirtualThread(data, perspectivesJson);

            aiReportCommandService.parseAndSave(mergedJson, data.experience(),
                    data.company(), command.jobDescription());

            reportJobService.complete(jobId);
            log.info("Job 처리 완료 - jobId: {}", jobId);
        } catch (Exception e) {
            log.error("Job 처리 실패 - jobId: {}", jobId, e);
            reportJobService.fail(jobId);
        }
    }

    private MatchExperienceCommandDto buildCommand(Long jobId) {
        AIReportJob job = reportJobService.findJob(jobId);
        return new MatchExperienceCommandDto(
                job.getUserId(),
                job.getCompanyId(),
                job.getExperienceId(),
                job.getDescription());
    }
}