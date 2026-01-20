package sopt.comfit.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.global.exception.CommonErrorCode;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum EEducationLevel {
    HIGH_SCHOOL("HIGH_SCHOOL", "고등학교 졸업"),
    BACHELOR_STUDENT("BACHELOR_STUDENT", "학사 재학"),
    BACHELOR("BACHELOR", "학사 졸업"),
    MASTER_STUDENT("MASTER_STUDENT", "석사 재학");

    private final String code;
    private final String description;

    public static EEducationLevel from(String value) {
        return Arrays.stream(values())
                .filter(educationLevel -> educationLevel.getCode().equals(value))
                .findFirst().orElseThrow(
                        () -> BaseException.type(CommonErrorCode.EDUCATION_LEVEL_NOT_FOUND)
                );
    }
}
