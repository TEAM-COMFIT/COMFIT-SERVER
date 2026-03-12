package sopt.comfit.report.job;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import sopt.comfit.global.constants.Constants;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("JobEventListener")
class JobEventListenerTest {

    @InjectMocks
    private JobEventListener listener;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ListOperations<String, String> listOperations;

    @Test
    @DisplayName("JobCreatedEvent 수신 시 Redis 큐에 jobId를 leftPush한다")
    void pushesJobIdToRedisQueueOnEvent() {
        // TransactionPhase.AFTER_COMMIT 이후에 Redis 큐에 job ID를 넣는 행위 검증
        // → 트랜잭션 성공 후에만 워커가 Job을 처리하도록 보장
        given(redisTemplate.opsForList()).willReturn(listOperations);

        JobCreatedEvent event = new JobCreatedEvent(42L);
        listener.handleJobCreated(event);

        // JOB_QUEUE_KEY에 "42" 문자열이 leftPush 되어야 함
        verify(listOperations).leftPush(
                eq(Constants.JOB_QUEUE_KEY),
                eq("42")
        );
    }

    @Test
    @DisplayName("서로 다른 jobId가 독립적으로 큐에 적재된다")
    void eachJobIdIsPushedIndependently() {
        given(redisTemplate.opsForList()).willReturn(listOperations);

        listener.handleJobCreated(new JobCreatedEvent(1L));
        listener.handleJobCreated(new JobCreatedEvent(2L));

        verify(listOperations).leftPush(eq(Constants.JOB_QUEUE_KEY), eq("1"));
        verify(listOperations).leftPush(eq(Constants.JOB_QUEUE_KEY), eq("2"));
    }
}
