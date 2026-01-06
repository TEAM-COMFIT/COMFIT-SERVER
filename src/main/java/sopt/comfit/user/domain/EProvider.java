package sopt.comfit.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EProvider {
    GOOGLE("구글"),
    KAKAO("카카오"),
    NORMAL("일반");

    private final String description;
}
