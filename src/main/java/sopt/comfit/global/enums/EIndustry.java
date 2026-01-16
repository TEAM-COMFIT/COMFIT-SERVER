package sopt.comfit.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.global.exception.EIndustryErrorCode;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum EIndustry {
    CONSUMER_GOODS("CONSUMER_GOODS","소비재/FMCG"),
    IT("IT","플랫폼/IT 서비스"),
    MEDIA_CONTENTS("MEDIA_CONTENTS","콘텐츠/미디어/엔터"),
    RETAIL("RETAIL","커머스/리테일"),
    LIFESTYLE("LIFESTYLE","패션,뷰티,라이프스타일"),
    FOOD("FOOD","푸드/F&B"),
    TRAVEL("TRAVEL","모빌리티/트래블/O2O"),
    FINANCE("FINANCE","금융/핀테크"),
    FITNESS("FITNESS","헬스케어/웰니스");

    private final String code;
    private final String description;

    public static EIndustry from(String value) {
        return Arrays.stream(values())
                .filter(eIndustry -> eIndustry.getCode().equals(value))
                .findFirst().orElseThrow(
                        () -> BaseException.type(EIndustryErrorCode.EINDUSTRY_NOT_FOUND)
                );
    }
}
