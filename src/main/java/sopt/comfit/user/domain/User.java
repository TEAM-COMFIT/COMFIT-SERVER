package sopt.comfit.user.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sopt.comfit.global.base.BaseTimeEntity;
import sopt.comfit.global.enums.EIndustry;
import sopt.comfit.university.domain.University;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    //인증 정보
    @Email
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password", length = 100)
    private String password;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private ERole role = ERole.USER;

    @Column(name = "provider", nullable = false)
    @Enumerated(EnumType.STRING)
    private EProvider provider;

    @Column(name = "provider_id")
    private String providerId;


    //기본 정보
    @Column(name = "name", nullable = false, length = 40)
    private String name;

    @Column(name = "education_level")
    @Enumerated(EnumType.STRING)
    private EEducationLevel educationLevel;


    //관심 산업군
    @Column(name = "first_industry")
    @Enumerated(EnumType.STRING)
    private EIndustry firstIndustry;

    @Column(name = "second_industry")
    @Enumerated(EnumType.STRING)
    private EIndustry secondIndustry;

    @Column(name = "third_industry")
    @Enumerated(EnumType.STRING)
    private EIndustry thirdIndustry;


    //관심 직무
    @Column(name = "first_job")
    @Enumerated(EnumType.STRING)
    private EJob firstJob;

    @Column(name = "second_job")
    @Enumerated(EnumType.STRING)
    private EJob secondJob;

    @Column(name = "third_job")
    @Enumerated(EnumType.STRING)
    private EJob thirdJob;

    // 약관 동의
    @Column(name = "terms_agreed")
    private boolean termsAgreed;

    @Column(name = "privacy_agreed")
    private boolean privacyAgreed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id")
    private University university;

    @Builder(access = AccessLevel.PRIVATE)
    private User(String email,
                 String password,
                 ERole role,
                 EProvider provider,
                 String providerId,
                 String name,
                 EEducationLevel educationLevel,
                 EIndustry firstIndustry,
                 EIndustry secondIndustry,
                 EIndustry thirdIndustry,
                 EJob firstJob,
                 EJob secondJob,
                 EJob thirdJob,
                 boolean termsAgreed,
                 boolean privacyAgreed,
                 University university) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.provider = provider;
        this.providerId = providerId;
        this.name = name;
        this.educationLevel = educationLevel;
        this.firstIndustry = firstIndustry;
        this.secondIndustry = secondIndustry;
        this.thirdIndustry = thirdIndustry;
        this.firstJob = firstJob;
        this.secondJob = secondJob;
        this.thirdJob = thirdJob;
        this.termsAgreed = termsAgreed;
        this.privacyAgreed = privacyAgreed;
        this.university = university;
    }

    public static User createKakaoUser(String email,
                                       String providerId,
                                       String name
    ) {
        return User.builder()
                .email(email)
                .role(ERole.USER)
                .provider(EProvider.KAKAO)
                .providerId(providerId)
                .name(name)
                .build();
    }

    public void registerRequiredInfo(
            String educationLevel,
            String firstIndustry,
            String secondIndustry,
            String thirdIndustry,
            String firstJob,
            String secondJob,
            String thirdJob,
            University university
    ) {
        this.educationLevel = EEducationLevel.from(educationLevel);
        this.firstIndustry = EIndustry.from(firstIndustry);
        this.secondIndustry = EIndustry.from(secondIndustry);
        this.thirdIndustry = EIndustry.from(thirdIndustry);
        this.firstJob = EJob.from(firstJob);
        this.secondJob = EJob.from(secondJob);
        this.thirdJob = EJob.from(thirdJob);
        this.university = university;
    }
}
