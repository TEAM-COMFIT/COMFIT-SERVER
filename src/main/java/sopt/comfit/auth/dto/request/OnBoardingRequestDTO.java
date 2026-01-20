package sopt.comfit.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import sopt.comfit.global.enums.EIndustry;
import sopt.comfit.user.domain.EEducationLevel;
import sopt.comfit.user.domain.EJob;

public record OnBoardingRequestDTO (

        @NotNull
        @Schema(example = "HIGH_SCHOOL")
        EEducationLevel educationLevel,


        @NotNull
        @Schema(example = "IT")
        EIndustry firstIndustry,

        @NotNull
        @Schema(example = "MEDIA_CONTENTS")
        EIndustry secondIndustry,

        @NotNull
        @Schema(example = "RETAIL")
        EIndustry thirdIndustry,

        @NotNull
        @Schema(example = "MARKETING_STRATEGY")
        EJob firstJob,

        @NotNull
        @Schema(example = "BRAND_MARKETING")
        EJob secondJob,

        @NotNull
        @Schema(example = "DIGITAL_MARKETING")
        EJob thirdJob,

        @NotNull
        @Schema(example = "1")
        Long universityId
) {
}
