package sopt.comfit.auth.dto.query;

import sopt.comfit.global.dto.JwtDto;

public record LoginQueryDto (
        Long id,
        boolean isNew,
        String name,
        JwtDto jwtDto
) {
    public static LoginQueryDto of(Long id, boolean isNew, String name, JwtDto jwtDto) {
        return new LoginQueryDto(id, isNew, name, jwtDto);
    }
}
