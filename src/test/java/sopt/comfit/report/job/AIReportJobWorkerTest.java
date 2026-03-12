package sopt.comfit.report.job;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import sopt.comfit.global.exception.BaseException;
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
}
