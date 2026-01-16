package sopt.comfit.auth.dto;

import sopt.comfit.global.dto.JwtDto;
import sopt.comfit.user.domain.User;

public record UserInfoDTO (
        Long id,
        boolean isNew,
        JwtDto jwtDto
) {
    public static UserInfoDTO from(User user, boolean isNew, JwtDto jwtDto) {
        return new UserInfoDTO(user.getId(), isNew, jwtDto);
    }
}
