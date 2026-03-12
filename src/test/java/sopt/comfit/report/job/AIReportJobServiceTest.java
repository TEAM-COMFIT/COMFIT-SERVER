package sopt.comfit.report.job;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import sopt.comfit.global.constants.Constants;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.report.domain.AIReportJob;
import sopt.comfit.report.domain.AIReportJobRepository;
import sopt.comfit.report.domain.EJobStatus;
import sopt.comfit.report.dto.command.MatchExperienceCommandDto;
import sopt.comfit.report.exception.AIReportErrorCode;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AIReportJobService")
class AIReportJobServiceTest {

    @InjectMocks
    private AIReportJobService jobService;

    @Mock
    private AIReportJobRepository jobRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ListOperations<String, String> listOperations;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    // ──────────────────────────────────────────────
    // createJob()
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("createJob()")
    class CreateJobTest {

        private final MatchExperienceCommandDto command =
                new MatchExperienceCommandDto(1L, 2L, 3L, "백엔드 개발자");

        @Test
        @DisplayName("큐 크기가 200 이하이면 Job을 생성하고 JobCreatedEvent를 발행한다")
        void createsJobAndPublishesEvent() {
            // given: 현재 큐에 100개 적재
            given(redisTemplate.opsForList()).willReturn(listOperations);
            given(listOperations.size(Constants.JOB_QUEUE_KEY)).willReturn(100L);
            given(jobRepository.save(any(AIReportJob.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            jobService.createJob(command);

            // then: 이벤트가 정확히 1회 발행되어야 함
            verify(eventPublisher, times(1)).publishEvent(any(JobCreatedEvent.class));
            verify(jobRepository, times(1)).save(any(AIReportJob.class));
        }

        @Test
        @DisplayName("큐 크기가 200 초과이면 JOB_QUEUE_FULL 예외를 던진다")
        void throwsWhenQueueFull() {
            // 트래픽 급증 시 큐가 포화 상태가 되는 경우 → 429 응답 유도
            given(redisTemplate.opsForList()).willReturn(listOperations);
            given(listOperations.size(Constants.JOB_QUEUE_KEY)).willReturn(201L);

            assertThatThrownBy(() -> jobService.createJob(command))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(AIReportErrorCode.JOB_QUEUE_FULL));

            // 큐가 가득 찬 경우 저장 및 이벤트 발행이 발생하지 않아야 함
            verify(jobRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("큐 크기가 정확히 200이면 Job을 생성한다 (경계값)")
        void createsJobAtQueueSizeBoundary() {
            // 경계값: queueSize > 200 조건이므로 200은 허용
            given(redisTemplate.opsForList()).willReturn(listOperations);
            given(listOperations.size(Constants.JOB_QUEUE_KEY)).willReturn(200L);
            given(jobRepository.save(any(AIReportJob.class))).willAnswer(inv -> inv.getArgument(0));

            jobService.createJob(command);

            verify(jobRepository, times(1)).save(any(AIReportJob.class));
        }

        @Test
        @DisplayName("Redis에서 null을 반환해도 Job을 생성한다 (Redis 연결 불안정 방어)")
        void createsJobWhenRedisSizeNull() {
            // Redis opsForList().size() 가 null 반환 시 예외 없이 처리
            given(redisTemplate.opsForList()).willReturn(listOperations);
            given(listOperations.size(Constants.JOB_QUEUE_KEY)).willReturn(null);
            given(jobRepository.save(any(AIReportJob.class))).willAnswer(inv -> inv.getArgument(0));

            jobService.createJob(command);

            verify(jobRepository, times(1)).save(any(AIReportJob.class));
        }

        @Test
        @DisplayName("생성된 Job의 초기 상태는 PENDING이다")
        void createdJobHasPendingStatus() {
            given(redisTemplate.opsForList()).willReturn(listOperations);
            given(listOperations.size(Constants.JOB_QUEUE_KEY)).willReturn(0L);

            ArgumentCaptor<AIReportJob> captor = ArgumentCaptor.forClass(AIReportJob.class);
            given(jobRepository.save(captor.capture())).willAnswer(inv -> inv.getArgument(0));

            jobService.createJob(command);

            AIReportJob saved = captor.getValue();
            assertThat(saved.getStatus()).isEqualTo(EJobStatus.PENDING);
            assertThat(saved.getUserId()).isEqualTo(1L);
            assertThat(saved.getCompanyId()).isEqualTo(2L);
            assertThat(saved.getExperienceId()).isEqualTo(3L);
        }
    }

    // ──────────────────────────────────────────────
    // findJob()
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("findJob()")
    class FindJobTest {

        @Test
        @DisplayName("존재하는 Job ID이면 Job을 반환한다")
        void returnsJobWhenFound() {
            AIReportJob job = AIReportJob.create(1L, 2L, 3L, "desc");
            given(jobRepository.findById(1L)).willReturn(Optional.of(job));

            AIReportJob result = jobService.findJob(1L);

            assertThat(result).isEqualTo(job);
        }

        @Test
        @DisplayName("존재하지 않는 Job ID이면 REPORT_JOB_NOT_FOUND 예외를 던진다")
        void throwsWhenJobNotFound() {
            given(jobRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> jobService.findJob(999L))
                    .isInstanceOf(BaseException.class)
                    .satisfies(e -> assertThat(((BaseException) e).getErrorCode())
                            .isEqualTo(AIReportErrorCode.REPORT_JOB_NOT_FOUND));
        }
    }

    // ──────────────────────────────────────────────
    // 상태 전이 (startProcessing / complete / fail)
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("Job 상태 전이")
    class StatusTransitionTest {

        @Test
        @DisplayName("startProcessing() 호출 시 상태가 PROCESSING으로 변경된다")
        void startProcessingTransition() {
            AIReportJob job = AIReportJob.create(1L, 2L, 3L, "desc");
            given(jobRepository.findById(1L)).willReturn(Optional.of(job));

            jobService.startProcessing(1L);

            assertThat(job.getStatus()).isEqualTo(EJobStatus.PROCESSING);
        }

        @Test
        @DisplayName("complete() 호출 시 상태가 COMPLETED로 변경된다")
        void completeTransition() {
            AIReportJob job = AIReportJob.create(1L, 2L, 3L, "desc");
            job.startProcessing();
            given(jobRepository.findById(1L)).willReturn(Optional.of(job));

            jobService.complete(1L);

            assertThat(job.getStatus()).isEqualTo(EJobStatus.COMPLETED);
        }

        @Test
        @DisplayName("fail() 호출 시 상태가 FAILED로 변경된다")
        void failTransition() {
            AIReportJob job = AIReportJob.create(1L, 2L, 3L, "desc");
            job.startProcessing();
            given(jobRepository.findById(1L)).willReturn(Optional.of(job));

            jobService.fail(1L);

            assertThat(job.getStatus()).isEqualTo(EJobStatus.FAILED);
        }
    }
}
