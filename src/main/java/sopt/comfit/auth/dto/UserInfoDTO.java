package sopt.comfit.auth.dto;

import sopt.comfit.global.dto.JwtDto;
import sopt.comfit.user.domain.User;

public record UserInfoDto(
        Long id,
        boolean isNew,
        JwtDto jwtDto
) {
    public static UserInfoDto from(User user, boolean isNew, JwtDto jwtDto) {
        return new UserInfoDto(user.getId(), isNew, jwtDto);
    }
}
