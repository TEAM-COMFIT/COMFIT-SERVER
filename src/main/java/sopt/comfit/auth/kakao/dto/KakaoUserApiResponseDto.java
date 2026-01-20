package sopt.comfit.auth.kakao.dto;

import sopt.comfit.global.dto.JwtDto;

public record KakaoUserApiResponseDto(
    Long id,
    KakaoAccount kakao_account,
    JwtDto jwtDto
) {
    public record KakaoAccount (
            KakaoProfile profile,
            String email
    ) {

    }

    public record KakaoProfile (
            String nickname
    ) {
    }
}
