package sopt.comfit.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum EJob {

    MARKETING_STRATEGY("마케팅전략/기획"),
    BRAND_MARKETING("브랜드마케팅"),
    DIGITAL_MARKETING("디지털마케팅"),
    CONTENT_MARKETING("콘텐츠마케팅"),
    VIRAL_INFLUENCER_MARKETING("바이럴/인플루언서마케팅"),
    PERFORMANCE_MARKETING("퍼포먼스마케팅"),
    B2B_MARKETING("B2B마케팅"),
    CRM_MARKETING("CRM마케팅"),
    PRODUCT_MARKETING("프로덕트마케팅"),
    PARTNERSHIP_MARKETING("제휴마케팅"),
    GLOBAL_MARKETING("글로벌마케팅");

    private final String description;

    public static EJob from(String value) {
        return Arrays.stream(values())
                .filter(eJob -> eJob.getDescription().equals(value))
                .findFirst().orElseGet(null);
    }
}
