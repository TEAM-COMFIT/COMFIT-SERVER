package sopt.comfit.company.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sopt.comfit.company.exception.CompanyErrorCode;
import sopt.comfit.global.enums.EIndustry;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.global.exception.CommonErrorCode;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum EScale {

    LARGE("LARGE", "대기업"),
    STARTUP("STARTUP","스타트업"),
    PUBLIC_CORP("PUBLIC_CORP", "공기업"),
    MID_LARGE("MID_LARGE", "중견기업"),
    SME("SME", "small and medium enterprise/중소기업"),
    FOREIGN("FOREIGN","외국기업"),
    PUBLIC_ORG("PUBLIC_ORG", "공공기관"),
    ETC("ETC", "기타");

    private final String code;
    private final String description;

    public static EScale from(String value) {

        if(value == null){
            return null;
        }
        return Arrays.stream(values())
                .filter(eScale -> eScale.getCode().equals(value))
                .findFirst().orElseThrow(
                        () -> BaseException.type(CompanyErrorCode.INVALID_KEYWORD)
                );
    }
}
