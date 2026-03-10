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
        stringRedisTemplate.opsForList().leftPush(
                Constants.JOB_QUEUE_KEY,
                String.valueOf(event.jobId())
        );
    }
}
