package sopt.comfit.report.dto.command;

import sopt.comfit.report.dto.request.MatchExperienceRequestDto;

public record MatchExperienceCommandDto(
        Long userId,

        Long companyId,

        Long experienceId

) {
    public static MatchExperienceCommandDto of(Long userId, MatchExperienceRequestDto request) {
        return new MatchExperienceCommandDto(userId, request.companyId(), request.experienceId());
    }
}
