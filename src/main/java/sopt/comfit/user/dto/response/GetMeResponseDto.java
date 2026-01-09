package sopt.comfit.user.dto.response;

import sopt.comfit.global.enums.EIndustry;
import sopt.comfit.user.domain.EEducationLevel;
import sopt.comfit.user.domain.EJob;
import sopt.comfit.user.domain.User;

public record GetMeResponseDto(

        String name,

        String email,

        EEducationLevel educationLevel,

        EIndustry firstIndustry,

        EJob fistJob
) {
    public static GetMeResponseDto from(User user) {
        return new GetMeResponseDto(
                user.getName(),
                user.getEmail(),
                user.getEducationLevel(),
                user.getFirstIndustry(),
                user.getFirstJob()
        );
    }
}
