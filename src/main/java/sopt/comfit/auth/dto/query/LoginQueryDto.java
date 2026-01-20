package sopt.comfit.auth.dto.query;

import sopt.comfit.global.dto.JwtDto;

public record LoginQueryDto (
        Long id,
        boolean isNew,
        JwtDto jwtDto
) {
    public static LoginQueryDto of(Long id, boolean isNew, JwtDto jwtDto) {
        return new LoginQueryDto(id, isNew, jwtDto);
    }
}
