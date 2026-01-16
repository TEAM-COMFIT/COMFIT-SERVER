package sopt.comfit.auth.dto;

import sopt.comfit.auth.dto.query.LoginQueryDto;

public record LoginResponseDto(
        Long id,
        boolean isNew,
        String accessToken
) {
    public static LoginResponseDto of(LoginQueryDto loginQueryDto) {
        return new LoginResponseDto(loginQueryDto.id(), loginQueryDto.isNew(), loginQueryDto.jwtDto().accessToken());
    }
}
