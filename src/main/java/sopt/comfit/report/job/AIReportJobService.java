package sopt.comfit.report.job;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sopt.comfit.global.constants.Constants;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.report.domain.AIReportJob;
import sopt.comfit.report.domain.AIReportJobRepository;
import sopt.comfit.report.exception.AIReportErrorCode;

@Service
@RequiredArgsConstructor
public class AIReportJobService {

    private final AIReportJobRepository reportJobRepository;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public Long createJob(Long userId, Long experienceId, Long companyId, String jobDescription) {
        AIReportJob job = AIReportJob.create(userId, experienceId, companyId, jobDescription);
        reportJobRepository.save(job);

        redisTemplate.opsForList().leftPush(Constants.JOB_QUEUE_KEY, String.valueOf(job.getId()));

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