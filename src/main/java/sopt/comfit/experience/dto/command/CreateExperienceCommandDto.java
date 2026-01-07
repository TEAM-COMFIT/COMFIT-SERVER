package sopt.comfit.experience.dto.command;

import sopt.comfit.experience.domain.EType;
import sopt.comfit.experience.dto.request.CreateExperienceRequestDto;

import java.time.LocalDate;

public record CreateExperienceCommandDto(
        Long userId,

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
    public static CreateExperienceCommandDto of(Long userId, CreateExperienceRequestDto request){
        return new CreateExperienceCommandDto(
                userId,
                request.title(),
                request.type(),
                request.startAt(),
                request.endAt(),
                request.situation(),
                request.task(),
                request.action(),
                request.result(),
                request.isDefault());
    }
}
