package sopt.comfit.user.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sopt.comfit.global.base.BaseTimeEntity;

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

    @Column(name = "education_level", nullable = false)
    @Enumerated(EnumType.STRING)
    private EEducationLevel educationLevel;


    //관심 산업군
    @Column(name = "first_industry", nullable = false)
    @Enumerated(EnumType.STRING)
    private EIndustry firstIndustry;

    @Column(name = "second_industry")
    @Enumerated(EnumType.STRING)
    private EIndustry secondIndustry;

    @Column(name = "third_industry")
    @Enumerated(EnumType.STRING)
    private EIndustry thirdIndustry;


    //관심 직무
    @Column(name = "first_job", nullable = false)
    @Enumerated(EnumType.STRING)
    private EJob firstJob;

    @Column(name = "second_job")
    @Enumerated(EnumType.STRING)
    private EJob secondJob;

    @Column(name = "third_job")
    @Enumerated(EnumType.STRING)
    private EJob thirdJob;

    // 약관 동의
    @Column(name = "terms_agreed", nullable = false)
    private boolean termsAgreed;

    @Column(name = "privacy_agreed", nullable = false)
    private boolean privacyAgreed;
}
