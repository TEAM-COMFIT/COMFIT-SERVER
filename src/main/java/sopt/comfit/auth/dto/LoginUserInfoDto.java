package sopt.comfit.auth.dto;

import sopt.comfit.global.dto.JwtDto;

public record LoginUserInfoDto(
        Long id,
        boolean isNew,
        JwtDto jwtDto
) {
    public static LoginUserInfoDto of(Long userId, boolean isNew, JwtDto jwtDto) {
        return new LoginUserInfoDto(userId, isNew, jwtDto);
    }
}
