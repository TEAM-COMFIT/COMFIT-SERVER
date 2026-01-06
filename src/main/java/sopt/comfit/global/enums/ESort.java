package sopt.comfit.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ESort {

    NAME("이름순"),
    LIKE("인기순"),
    LATEST("최신순(스크랩순)"),
    OLDEST("오래된 순");

    private final String description;
}
