package sopt.comfit.company.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sopt.comfit.global.base.BaseTimeEntity;
import sopt.comfit.global.enums.EIndustry;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "companies")
public class Company extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "summary", nullable = false)
    private String summary;

    @Column(name = "talent_profile", nullable = false, columnDefinition = "TEXT")
    private String talentProfile;

    @Column(name = "scale", nullable = false)
    @Enumerated(EnumType.STRING)
    private EScale scale;

    @Column(name = "industry", nullable = false)
    @Enumerated(EnumType.STRING)
    private EIndustry industry;

    @Column(name = "is_recruiting", nullable = false)
    private boolean isRecruiting;

    @Column(name = "job_description", columnDefinition = "TEXT")
    private String jobDescription;

    //url
    @Column(name = "logo")
    private String logo;

    @Column(name = "company_url", nullable = false, columnDefinition = "TEXT")
    private String companyUrl;

    @Column(name= "recruit_url", columnDefinition = "TEXT")
    private String recruitUrl;
}
