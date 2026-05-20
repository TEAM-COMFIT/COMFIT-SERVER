package sopt.comfit.report.job;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import sopt.comfit.global.constants.Constants;

@Component
@RequiredArgsConstructor
public class JobEventListener {

    private final StringRedisTemplate stringRedisTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleJobCreated(JobCreatedEvent event) {
        // traceparent가 있으면 "jobId|traceparent" 형태로 저장해 Worker가 parent trace에 연결할 수 있도록
        String value = event.traceparent() != null
                ? event.jobId() + "|" + event.traceparent()
                : String.valueOf(event.jobId());
        stringRedisTemplate.opsForList().leftPush(Constants.JOB_QUEUE_KEY, value);
    }
}
