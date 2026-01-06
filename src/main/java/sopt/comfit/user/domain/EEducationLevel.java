package sopt.comfit.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EEducationLevel {
    HIGH_SCHOOL("고졸"),
    BACHELOR_STUDENT("학사 재학"),
    BACHELOR("학사 졸업"),
    MASTER_STUDENT("석사 재학"),
    MASTER("석사 졸업"),
    DOCTOR_STUDENT("박사 재학"),
    DOCTOR("박사");

    private final String description;
}
