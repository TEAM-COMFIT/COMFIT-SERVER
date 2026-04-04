package sopt.comfit.report.job;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import sopt.comfit.global.constants.Constants;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.global.logging.MdcUtils;
import sopt.comfit.report.domain.AIReportJob;
import sopt.comfit.report.dto.command.MatchExperienceCommandDto;
import sopt.comfit.report.infra.dto.PreparedDataDto;
import sopt.comfit.report.infra.prompt.AIReportParallelPromptBuilder;
import sopt.comfit.report.infra.service.RetryableAiCallerService;
import sopt.comfit.report.service.AIReportCommandService;
import sopt.comfit.report.service.AIReportQueryService;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class AIReportJobWorker {

    private static final int WORKER_COUNT = 2;

    private final StringRedisTemplate redisTemplate;
    private final AIReportJobService reportJobService;
    private final AIReportQueryService aiReportQueryService;
    private final AIReportCommandService aiReportCommandService;
    private final RetryableAiCallerService aiCaller;

    private final AtomicBoolean running = new AtomicBoolean(false);
    List<Thread> workers = new CopyOnWriteArrayList<>();

    @PostConstruct
    public void startWorker() {
        running.set(true);
        workers = new CopyOnWriteArrayList<>();
        for (int i = 0; i < WORKER_COUNT; i++) {
            spawnWorker(i);
        }
        log.info("ReportJobWorker {}개 시작", WORKER_COUNT);
    }

    @PreDestroy
    public void stopWorker() {
        running.set(false);
        workers.forEach(Thread::interrupt);
        log.info("ReportJobWorker 종료");
    }

    private void spawnWorker(int index) {
        Thread t = Thread.ofVirtual()
                .name("report-job-worker-" + index)
                .start(() -> guardedListen(index));
        workers.add(t);
    }

    /**
     * listen()을 감싸서 Error 포함 Throwable이 발생해 스레드가 죽어도
     * running 상태면 자동으로 재시작한다.
     */
    private void guardedListen(int index) {
        while (running.get()) {
            try {
                listen();
            } catch (Throwable t) {
                if (!running.get()) break;
                log.error("Worker-{} 치명적 오류로 종료, 3초 후 재시작", index, t);
                sleep(3);
            }
        }
        log.info("Worker-{} 정상 종료", index);
    }

    private void listen() {
        while (!Thread.currentThread().isInterrupted() && running.get()) {
            try {
                String jobId = redisTemplate.opsForList()
                        .rightPop(Constants.JOB_QUEUE_KEY, Duration.ofSeconds(30));
                if (jobId == null) continue;
                processJob(Long.parseLong(jobId));

            } catch (QueryTimeoutException e) {
                log.debug("BRPOP timeout, 재시도");

            } catch (Exception e) {
                log.error("Worker 루프 에러", e);
                sleep(3); // Redis 장애 시 스핀 방지
            }
        }
    }

    private void sleep(int seconds) {
        try {
            Thread.sleep(Duration.ofSeconds(seconds));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void processJob(Long jobId) {

        try {
            //MDC 설정
            MdcUtils.generateTraceId();
            MdcUtils.setJobId(jobId);

            log.info("Job 처리 시작");
            reportJobService.startProcessing(jobId);

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

        } catch (BaseException e) {
            log.warn("Job 실패 - jobId={}", jobId, e);
            reportJobService.fail(jobId);

        } catch (Exception e) {
            log.error("Job 시스템 오류 - jobId={}", jobId, e);
            reportJobService.fail(jobId);

        } finally {
            MdcUtils.clear();
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