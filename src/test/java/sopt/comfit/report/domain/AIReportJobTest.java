package sopt.comfit.report.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AIReportJob 도메인 상태 전이")
class AIReportJobTest {

    // Job의 수명 주기: PENDING → PROCESSING → COMPLETED or FAILED
    // 각 상태 전이가 명확히 동작하는지 도메인 레벨에서 검증

    @Test
    @DisplayName("create() 호출 시 초기 상태는 PENDING이다")
    void createJobWithPendingStatus() {
        AIReportJob job = AIReportJob.create(1L, 2L, 3L, "백엔드 개발");

        assertThat(job.getUserId()).isEqualTo(1L);
        assertThat(job.getCompanyId()).isEqualTo(2L);
        assertThat(job.getExperienceId()).isEqualTo(3L);
        assertThat(job.getDescription()).isEqualTo("백엔드 개발");
        assertThat(job.getStatus()).isEqualTo(EJobStatus.PENDING);
    }

    @Test
    @DisplayName("startProcessing() 호출 시 상태가 PROCESSING으로 변경된다")
    void startProcessingChangesStatusToProcessing() {
        // Worker가 Job을 Redis 큐에서 꺼낸 직후 상태 변경
        AIReportJob job = AIReportJob.create(1L, 2L, 3L, "desc");

        job.startProcessing();

        assertThat(job.getStatus()).isEqualTo(EJobStatus.PROCESSING);
    }

    @Test
    @DisplayName("complete() 호출 시 상태가 COMPLETED로 변경된다")
    void completeChangesStatusToCompleted() {
        // AI 응답 파싱 및 저장 성공 후 상태 변경
        AIReportJob job = AIReportJob.create(1L, 2L, 3L, "desc");
        job.startProcessing();

        job.complete();

        assertThat(job.getStatus()).isEqualTo(EJobStatus.COMPLETED);
    }

    @Test
    @DisplayName("fail() 호출 시 상태가 FAILED로 변경된다")
    void failChangesStatusToFailed() {
        // BaseException 또는 시스템 오류 발생 시 상태 변경
        AIReportJob job = AIReportJob.create(1L, 2L, 3L, "desc");
        job.startProcessing();

        job.fail();

        assertThat(job.getStatus()).isEqualTo(EJobStatus.FAILED);
    }

    @Test
    @DisplayName("PENDING 상태에서도 바로 fail()을 호출할 수 있다")
    void canFailFromPendingState() {
        // 처리 시작 전 예외 발생 시나리오 (예: DB 오류로 startProcessing 실패)
        AIReportJob job = AIReportJob.create(1L, 2L, 3L, "desc");

        job.fail();

        assertThat(job.getStatus()).isEqualTo(EJobStatus.FAILED);
    }
}
