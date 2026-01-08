package sopt.comfit.experience.dto.response;

import sopt.comfit.experience.domain.EType;
import sopt.comfit.experience.domain.Experience;

import java.time.LocalDate;

public record GetExperienceResponseDto(
        String title,

        boolean isDefault,

        EType type,

        LocalDate startAt,

        LocalDate endAt,

        String situation,

        String task,

        String action,

        String result
) {
    public static GetExperienceResponseDto from(Experience experience) {
        return new GetExperienceResponseDto(
                experience.getTitle(),
                experience.isDefault(),
                experience.getType(),
                experience.getStartAt(),
                experience.getEndAt(),
                experience.getSituation(),
                experience.getTask(),
                experience.getAction(),
                experience.getResult()
        );
    }
}
