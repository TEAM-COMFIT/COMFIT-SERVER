package sopt.comfit.company.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EScale {

    LARGE("대기업"),
    STARTUP("스타트업"),
    PUBLIC_CORP("공기업"),
    MID_LARGE("중견기업"),
    SME("small and medium enterprise/중소기업"),
    FOREIGN("외국기업"),
    PUBLIC_ORG("공공기관"),
    ETC("기타");

    private final String description;
}
