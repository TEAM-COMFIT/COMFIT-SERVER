package sopt.comfit.experience.dto.command;

import sopt.comfit.experience.domain.EType;
import sopt.comfit.experience.dto.request.ExperienceRequestDto;

import java.time.LocalDate;

public record UpdateExperienceCommandDto(
        Long userId,

        Long experienceId,

        String title,

        EType type,

        LocalDate startAt,

        LocalDate endAt,

        String situation,

        String task,

        String action,

        String result,

        boolean isDefault
) {
    public static UpdateExperienceCommandDto of(Long userId, Long experienceId, ExperienceRequestDto request){
        return new UpdateExperienceCommandDto(
                userId,
                experienceId,
                request.title(),
                request.type(),
                request.startAt(),
                request.endAt(),
                request.situation(),
                request.task(),
                request.action(),
                request.result(),
                request.isDefault()
        );
    }
}
