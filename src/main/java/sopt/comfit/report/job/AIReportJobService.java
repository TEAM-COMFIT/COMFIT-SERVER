package sopt.comfit.report.job;

import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sopt.comfit.global.constants.Constants;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.report.domain.AIReportJob;
import sopt.comfit.report.domain.AIReportJobRepository;
import sopt.comfit.report.dto.command.MatchExperienceCommandDto;
import sopt.comfit.report.exception.AIReportErrorCode;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIReportJobService {

    private final AIReportJobRepository reportJobRepository;
    private final StringRedisTemplate redisTemplate;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Long createJob(MatchExperienceCommandDto command) {

        Long queueSize = redisTemplate.opsForList().size(Constants.JOB_QUEUE_KEY);

        if (queueSize != null && queueSize > 200) {
            throw BaseException.type(AIReportErrorCode.JOB_QUEUE_FULL);
        }
        AIReportJob job = AIReportJob.create(
                command.userId(),
                command.companyId(),
                command.experienceId(),
                command.jobDescription());

        reportJobRepository.save(job);

        // 현재 HTTP 요청의 trace context를 W3C traceparent 포맷으로 추출
        // Worker에서 복원해 job.process span을 HTTP 요청 trace의 child로 연결
        Map<String, String> carrier = new HashMap<>();
        W3CTraceContextPropagator.getInstance().inject(Context.current(), carrier, Map::put);
        String traceparent = carrier.get("traceparent");

        eventPublisher.publishEvent(new JobCreatedEvent(job.getId(), traceparent));

        return job.getId();
    }

    @Transactional
    public void startProcessing(Long jobId) {
        AIReportJob job = findJob(jobId);
        job.startProcessing();
    }

    @Transactional
    public void complete(Long jobId) {
        AIReportJob job = findJob(jobId);
        job.complete();
    }

    @Transactional
    public void fail(Long jobId) {
        AIReportJob job = findJob(jobId);
        job.fail();
    }

    @Transactional(readOnly = true)
    public AIReportJob findJob(Long jobId) {
        return reportJobRepository.findById(jobId)
                .orElseThrow(() -> BaseException.type(AIReportErrorCode.REPORT_JOB_NOT_FOUND));
    }
}