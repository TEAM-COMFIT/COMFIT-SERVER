package sopt.comfit.report.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sopt.comfit.global.base.BaseTimeEntity;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "ai_report_job")
public class AIReportJob extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long experienceId;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EJobStatus status =  EJobStatus.PENDING;

    @Builder(access = AccessLevel.PRIVATE)
    private AIReportJob(
            final Long userId,
            final Long experienceId,
            final Long companyId,
            final String description,
            final EJobStatus status
    ){
        this.userId = userId;
        this.experienceId = experienceId;
        this.companyId = companyId;
        this.description = description;
        this.status = status;
    }

    public static AIReportJob create(final Long userId,
                                     final Long experienceId,
                                     final Long companyId,
                                     final String description) {
        return new AIReportJob(
                userId,
                experienceId,
                companyId,
                description,
                EJobStatus.PENDING);
    }

    public void startProcessing() {
        this.status = EJobStatus.PROCESSING;
    }

    public void complete() {
        this.status = EJobStatus.COMPLETED;
    }

    public void fail() {
        this.status = EJobStatus.FAILED;
    }
}
