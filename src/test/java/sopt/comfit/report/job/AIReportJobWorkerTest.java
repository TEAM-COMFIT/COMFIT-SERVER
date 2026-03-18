package sopt.comfit.report.job;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.global.logging.MdcUtils;
import sopt.comfit.report.domain.AIReportJob;
import sopt.comfit.report.dto.command.MatchExperienceCommandDto;
import sopt.comfit.report.exception.AIReportErrorCode;
import sopt.comfit.report.infra.dto.PreparedDataDto;
import sopt.comfit.report.infra.prompt.AIReportParallelPromptBuilder;
import sopt.comfit.report.infra.service.RetryableAiCallerService;
import sopt.comfit.report.service.AIReportCommandService;
import sopt.comfit.report.service.AIReportQueryService;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AIReportJobWorker - processJob()")
class AIReportJobWorkerTest {

    // processJob()은 private이지만 핵심 비즈니스 로직이므로 리플렉션으로 직접 테스트
    // AIReportParallelPromptBuilder는 static 메서드이므로 mockStatic으로 격리

    @InjectMocks
    private AIReportJobWorker worker;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private AIReportJobService reportJobService;

    @Mock
    private AIReportQueryService aiReportQueryService;

    @Mock
    private AIReportCommandService aiReportCommandService;

    @Mock
    private RetryableAiCallerService aiCaller;

    @Mock
    private ListOperations<String, String> listOperations;

    // processJob()을 리플렉션으로 호출하는 헬퍼
    private void invokeProcessJob(Long jobId) throws Exception {
        Method method = AIReportJobWorker.class.getDeclaredMethod("processJob", Long.class);
        method.setAccessible(true);
        method.invoke(worker, jobId);
    }

    @Test
    @DisplayName("정상 흐름: startProcessing → AI 호출 → parseAndSave → complete")
    void processJobSuccessfully() throws Exception {
        Long jobId = 1L;
        AIReportJob job = AIReportJob.create(10L, 20L, 30L, "백엔드");
        PreparedDataDto preparedData = mock(PreparedDataDto.class);

        given(reportJobService.findJob(jobId)).willReturn(job);
        given(aiReportQueryService.prepareData(any(MatchExperienceCommandDto.class)))
                .willReturn(preparedData);

        String fakePersp = "{\"perspectives\": []}";
        String fakeMerged = "{\"perspectives\":[],\"density\":[],\"appealPoint\":[],\"suggestion\":\"s\",\"guidance\":\"g\"}";

        // AIReportParallelPromptBuilder의 static 메서드를 격리 (내부에서 Company/Experience 메서드 직접 호출)
        try (MockedStatic<AIReportParallelPromptBuilder> promptMock =
                     mockStatic(AIReportParallelPromptBuilder.class)) {

            promptMock.when(() -> AIReportParallelPromptBuilder.buildPerspective(preparedData))
                    .thenReturn("perspective-prompt");

            given(aiCaller.callSyncWithField("perspective-prompt", "Perspectives", "perspectives"))
                    .willReturn(fakePersp);
            given(aiCaller.callParallelWithVirtualThread(preparedData, fakePersp))
                    .willReturn(fakeMerged);

            // when
            invokeProcessJob(jobId);
        }

        // then: 상태 전이가 순서대로 호출되어야 함
        verify(reportJobService).startProcessing(jobId);
        verify(aiReportCommandService).parseAndSave(anyString(), any(), any(), anyString());
        verify(reportJobService).complete(jobId);
        verify(reportJobService, never()).fail(jobId);
    }

    @Test
    @DisplayName("BaseException 발생 시 Job 상태를 FAILED로 변경하고 complete는 호출하지 않는다")
    void failsJobOnBaseException() throws Exception {
        // AI 호출 중 서킷브레이커 OPEN, 파싱 실패 등 비즈니스 예외 발생 시나리오
        Long jobId = 2L;
        AIReportJob job = AIReportJob.create(10L, 20L, 30L, "desc");
        PreparedDataDto preparedData = mock(PreparedDataDto.class);

        given(reportJobService.findJob(jobId)).willReturn(job);
        given(aiReportQueryService.prepareData(any())).willReturn(preparedData);

        try (MockedStatic<AIReportParallelPromptBuilder> promptMock =
                     mockStatic(AIReportParallelPromptBuilder.class)) {

            promptMock.when(() -> AIReportParallelPromptBuilder.buildPerspective(preparedData))
                    .thenReturn("perspective-prompt");

            given(aiCaller.callSyncWithField(anyString(), anyString(), anyString()))
                    .willThrow(BaseException.type(AIReportErrorCode.AI_SERVICE_UNAVAILABLE));

            // when
            invokeProcessJob(jobId);
        }

        // then: 예외 발생 시 fail()이 호출되어 데이터 정합성 유지
        verify(reportJobService).fail(jobId);
        verify(reportJobService, never()).complete(jobId);
    }

    @Test
    @DisplayName("예상치 못한 RuntimeException 발생 시 Job 상태를 FAILED로 변경한다")
    void failsJobOnUnexpectedException() throws Exception {
        // NullPointerException 등 시스템 오류도 catch (Exception e) 블록에서 fail()로 처리
        Long jobId = 3L;
        AIReportJob job = AIReportJob.create(10L, 20L, 30L, "desc");

        given(reportJobService.findJob(jobId)).willReturn(job);
        given(aiReportQueryService.prepareData(any()))
                .willThrow(new RuntimeException("DB 연결 오류"));

        // when
        invokeProcessJob(jobId);

        // then
        verify(reportJobService).fail(jobId);
        verify(reportJobService, never()).complete(jobId);
    }

    @Test
    @DisplayName("startWorker()는 2개의 가상 스레드 워커를 시작한다")
    void startWorkerCreatesVirtualThreads() {
        // @PostConstruct 동작 검증: 워커 스레드 수 확인
        given(redisTemplate.opsForList()).willReturn(listOperations);
        given(listOperations.rightPop(anyString(), any(Duration.class))).willReturn(null);

        worker.startWorker();

        assertThat(worker.workers).hasSize(2);

        worker.stopWorker();
    }

    // ──────────────────────────────────────────────
    // MDC 컨텍스트 검증
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("MDC 컨텍스트")
    class MdcContextTest {

        @Test
        @DisplayName("processJob() 실행 중 traceId와 jobId가 MDC에 설정되고, 완료 후 반드시 초기화된다")
        void mdcIsSetDuringProcessingAndClearedAfter() throws Exception {
            // MDC 설정 → 처리 → finally에서 clear()가 보장되는지 검증
            // 가상 스레드가 MDC를 오염시키지 않도록 clear()가 필수임을 확인
            Long jobId = 10L;
            AIReportJob job = AIReportJob.create(10L, 20L, 30L, "백엔드");
            PreparedDataDto preparedData = mock(PreparedDataDto.class);

            // startProcessing() 호출 시점(MDC 설정 직후)에 MDC 스냅샷을 캡처
            String[] capturedTraceId = new String[1];
            String[] capturedJobId  = new String[1];

            given(reportJobService.findJob(jobId)).willReturn(job);
            given(aiReportQueryService.prepareData(any(MatchExperienceCommandDto.class)))
                    .willReturn(preparedData);

            doAnswer(inv -> {
                // MdcUtils.generateTraceId() + setJobId()가 호출된 직후 시점
                capturedTraceId[0] = MDC.get(MdcUtils.TRACE_ID);
                capturedJobId[0]   = MDC.get(MdcUtils.JOB_ID);
                return null;
            }).when(reportJobService).startProcessing(jobId);

            try (MockedStatic<AIReportParallelPromptBuilder> promptMock =
                         mockStatic(AIReportParallelPromptBuilder.class)) {

                promptMock.when(() -> AIReportParallelPromptBuilder.buildPerspective(preparedData))
                        .thenReturn("perspective-prompt");
                given(aiCaller.callSyncWithField(anyString(), anyString(), anyString()))
                        .willReturn("{\"perspectives\": []}");
                given(aiCaller.callParallelWithVirtualThread(any(), anyString()))
                        .willReturn("{\"perspectives\":[],\"density\":[],\"appealPoint\":[],"
                                + "\"suggestion\":\"s\",\"guidance\":\"g\"}");

                invokeProcessJob(jobId);
            }

            // 처리 중: traceId는 8자리 UUID prefix, jobId는 정확히 매핑되어야 함
            assertThat(capturedTraceId[0])
                    .as("처리 중 MDC traceId가 설정되어 있어야 한다")
                    .isNotNull()
                    .hasSize(8);
            assertThat(capturedJobId[0])
                    .as("처리 중 MDC jobId가 정확히 설정되어 있어야 한다")
                    .isEqualTo(String.valueOf(jobId));

            // 처리 후: finally 블록의 MdcUtils.clear()로 반드시 비워져야 함
            // 비워지지 않으면 같은 가상 스레드가 다음 Job을 처리할 때 이전 traceId가 섞임
            assertThat(MDC.get(MdcUtils.TRACE_ID))
                    .as("처리 완료 후 MDC traceId가 초기화되어야 한다")
                    .isNull();
            assertThat(MDC.get(MdcUtils.JOB_ID))
                    .as("처리 완료 후 MDC jobId가 초기화되어야 한다")
                    .isNull();
        }

        @Test
        @DisplayName("processJob() 실패 시 로그 이벤트에 jobId와 traceId가 포함된다")
        void failureLogContainsJobIdAndTraceId() throws Exception {
            // 장애 발생 시 로그에 MDC 컨텍스트(jobId, traceId)가 포함되는지 검증
            // → 운영 중 특정 Job의 실패 원인을 traceId로 추적 가능해야 함
            Long jobId = 11L;
            AIReportJob job = AIReportJob.create(10L, 20L, 30L, "desc");
            PreparedDataDto preparedData = mock(PreparedDataDto.class);

            // Logback ListAppender로 AIReportJobWorker 로그 이벤트 캡처
            ch.qos.logback.classic.Logger workerLogger =
                    (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(AIReportJobWorker.class);
            ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
            listAppender.start();
            workerLogger.addAppender(listAppender);

            try {
                given(reportJobService.findJob(jobId)).willReturn(job);
                given(aiReportQueryService.prepareData(any())).willReturn(preparedData);

                try (MockedStatic<AIReportParallelPromptBuilder> promptMock =
                             mockStatic(AIReportParallelPromptBuilder.class)) {

                    promptMock.when(() -> AIReportParallelPromptBuilder.buildPerspective(preparedData))
                            .thenReturn("perspective-prompt");
                    // AI 호출에서 BaseException 발생 → "Job 실패" warn 로그 트리거
                    given(aiCaller.callSyncWithField(anyString(), anyString(), anyString()))
                            .willThrow(BaseException.type(AIReportErrorCode.AI_SERVICE_UNAVAILABLE));

                    invokeProcessJob(jobId);
                }

                // "Job 실패" warn 로그 이벤트 검색
                ILoggingEvent failLog = listAppender.list.stream()
                        .filter(e -> e.getFormattedMessage().contains("Job 실패"))
                        .findFirst()
                        .orElseThrow(() -> new AssertionError(
                                "'Job 실패' 로그 이벤트가 캡처되지 않음. "
                                + "실제 로그: " + listAppender.list.stream()
                                        .map(ILoggingEvent::getFormattedMessage).toList()));

                // 로그 이벤트가 캡처될 당시(finally 전)의 MDC 값 확인
                Map<String, String> mdc = failLog.getMDCPropertyMap();

                assertThat(mdc.get(MdcUtils.JOB_ID))
                        .as("실패 로그에 jobId(%d)가 MDC에 포함되어야 한다", jobId)
                        .isEqualTo(String.valueOf(jobId));
                assertThat(mdc.get(MdcUtils.TRACE_ID))
                        .as("실패 로그에 traceId가 MDC에 포함되어야 한다")
                        .isNotNull()
                        .hasSize(8);

            } finally {
                // 다른 테스트에 영향을 주지 않도록 appender 반드시 해제
                workerLogger.detachAppender(listAppender);
            }
        }
    }

    // ──────────────────────────────────────────────
    // Redis 복원력 검증
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("Redis 복원력")
    class RedisResilienceTest {

        @Test
        @DisplayName("Redis 연결 실패 시 Worker 루프가 종료되지 않고 재시도한다")
        void workerContinuesAfterRedisConnectionFailure() throws InterruptedException {
            // listen() 루프의 catch (Exception e) 블록이 RedisConnectionFailureException을
            // 삼키고 루프를 이어가는지 검증
            // → Redis 순단 발생 시 Worker 스레드가 죽어 이후 모든 Job 처리가 중단되는 버그 방지
            given(redisTemplate.opsForList()).willReturn(listOperations);

            AtomicInteger callCount = new AtomicInteger(0);
            // 재시도(2번째 rightPop 호출)가 발생했을 때 신호를 받기 위한 래치
            CountDownLatch retryLatch = new CountDownLatch(1);

            given(listOperations.rightPop(anyString(), any(Duration.class)))
                    .willAnswer(inv -> {
                        int count = callCount.incrementAndGet();

                        if (count == 1) {
                            // 첫 번째 호출: Redis 연결 실패 시뮬레이션
                            // listen()의 catch (Exception e) 블록이 이 예외를 처리해야 함
                            throw new RedisConnectionFailureException("Connection refused: localhost:6379");
                        }

                        // 두 번째 이상 호출: 재시도 발생 신호 후 스레드 인터럽트 대기
                        // 인터럽트 전까지 블로킹하여 루프가 무한 반복되지 않도록 제어
                        retryLatch.countDown();
                        try {
                            Thread.sleep(Long.MAX_VALUE);
                        } catch (InterruptedException e) {
                            // stopWorker()의 interrupt()를 받으면 while 조건 탈출을 위해 재설정
                            Thread.currentThread().interrupt();
                        }
                        return null;
                    });

            worker.startWorker();

            // 최대 3초 안에 재시도가 발생해야 함
            boolean retried = retryLatch.await(3, TimeUnit.SECONDS);
            worker.stopWorker();

            assertThat(retried)
                    .as("Redis 연결 실패 후 Worker가 루프를 유지하며 재시도해야 한다")
                    .isTrue();
            assertThat(callCount.get())
                    .as("rightPop이 최소 2회(실패 1회 + 재시도 1회) 호출되어야 한다")
                    .isGreaterThanOrEqualTo(2);
        }
    }
}
