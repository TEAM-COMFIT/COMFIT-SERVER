package sopt.comfit.report.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import sopt.comfit.company.domain.Company;
import sopt.comfit.experience.domain.Experience;
import sopt.comfit.global.base.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ai_reports")
public class AIReport extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_description", nullable = false, columnDefinition = "TEXT")
    private String jobDescription;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "perspectives", nullable = false, columnDefinition = "JSONB")
    private String perspectives;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "density", nullable = false, columnDefinition = "JSONB")
    private String density;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "appeal_point", nullable = false, columnDefinition = "JSONB")
    private String appealPoint;

    @Column(name = "suggestion", nullable = false, columnDefinition = "TEXT")
    private String suggestion;

    @Column(name = "guidance", nullable = false, columnDefinition = "TEXT")
    private String guidance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experience_id", nullable = false)
    private Experience experience;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Builder(access = AccessLevel.PROTECTED)
    private AIReport(
                     final String jobDescription,
                     final String perspectives,
                     final String density,
                     final String appealPoint,
                     final String suggestion,
                     final String guidance,
                     final Experience experience,
                     final Company company) {
        this.jobDescription = jobDescription;
        this.perspectives = perspectives;
        this.density = density;
        this.appealPoint = appealPoint;
        this.suggestion = suggestion;
        this.guidance = guidance;
        this.experience = experience;
        this.company = company;
    }

    public static AIReport create(
            final String jobDescription,
            final String perspectives,
            final String density,
            final String appealPoint,
            final String suggestion,
            final String guidance,
            final Experience experience,
            final Company company) {

        return AIReport.builder()
                .jobDescription(jobDescription)
                .perspectives(perspectives)
                .density(density)
                .appealPoint(appealPoint)
                .suggestion(suggestion)
                .guidance(guidance)
                .experience(experience)
                .company(company)
                .build();
    }



}
