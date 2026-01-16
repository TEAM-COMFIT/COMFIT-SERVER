package sopt.comfit.auth.dto;

import sopt.comfit.global.dto.JwtDto;

public record KakaoUserApiResponseDTO(
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
