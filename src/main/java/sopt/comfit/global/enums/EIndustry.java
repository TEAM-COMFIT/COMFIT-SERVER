package sopt.comfit.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EIndustry {
    CONSUMER_GOODS("소비재/FMCG"),
    IT("플랫폼/IT 서비스"),
    MEDIA_CONTENTS("콘텐츠/미디어/엔터"),
    RETAIL("커머스/리테일"),
    LIFESTYLE("패션,뷰티,라이프스타일"),
    FOOD("푸드/F&B"),
    TRAVEL("모빌리티/트래블/O2O"),
    FINANCE("금융/핀테크"),
    FITNESS("헬스케어/웰니스");

    private final String description;
}
