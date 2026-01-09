package sopt.comfit.report.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record MatchExperienceRequestDto(
        @NotNull
        @Schema(example = "1")
        Long companyId,

        @NotNull
        @Schema(example = "1")
        Long experienceId
) {
}
