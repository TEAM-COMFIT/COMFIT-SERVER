package sopt.comfit.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OnBoardingRequestDTO (

        @NotBlank
        @Schema(example = "HIGH_SCHOOL")
        String educationLevel,

        @NotBlank
        @Schema(example = "IT")
        String firstIndustry,

        @Schema(example = "MEDIA_CONTENTS")
        String secondIndustry,

        @Schema(example = "RETAIL")
        String thirdIndustry,

        @NotNull
        @Schema(example = "MARKETING_STRATEGY")
        String firstJob,

        @Schema(example = "BRAND_MARKETING")
        String secondJob,

        @Schema(example = "DIGITAL_MARKETING")
        String thirdJob,

        @NotNull
        @Schema(example = "1")
        Long universityId
) {
}
