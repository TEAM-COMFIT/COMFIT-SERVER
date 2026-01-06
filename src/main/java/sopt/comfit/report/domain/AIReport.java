package sopt.comfit.report.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sopt.comfit.company.domain.Company;
import sopt.comfit.global.base.BaseTimeEntity;
import sopt.comfit.user.domain.User;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ai_reports")
public class AIReport extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "perspectives", nullable = false, columnDefinition = "JSONB")
    private String perspectives;

    @Column(name = "density", nullable = false, columnDefinition = "TEXT")
    private String density;

    @Column(name = "appeal_point", nullable = false, columnDefinition = "JSONB")
    private String appealPoint;

    @Column(name = "suggestion", nullable = false, columnDefinition = "TEXT")
    private String suggestion;

    @Column(name = "guidance", nullable = false, columnDefinition = "TEXT")
    private String guidance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}
