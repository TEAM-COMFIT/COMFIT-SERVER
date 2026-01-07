package sopt.comfit.experience.dto.response;

import sopt.comfit.experience.domain.EType;
import sopt.comfit.experience.domain.Experience;

import java.time.LocalDate;

public record GetSummaryExperienceResponseDto(
        Long id,
        String title,
        EType type,
        boolean isDefault,
        LocalDate updatedAt
) {
    public static GetSummaryExperienceResponseDto from(Experience experience) {
        return new GetSummaryExperienceResponseDto(
                experience.getId(),
                experience.getTitle(),
                experience.getType(),
                experience.isDefault(),
                experience.getUpdatedAt().toLocalDate()
        );
    }
}
