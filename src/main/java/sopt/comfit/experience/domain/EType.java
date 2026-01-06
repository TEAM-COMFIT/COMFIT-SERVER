package sopt.comfit.experience.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EType {

    INTERNSHIP("인턴/실무"),
    PROJECT("공모전/프로젝트"),
    EDUCATION("수업/교육"),
    ETC("개인활동");

    private final String description;
}
