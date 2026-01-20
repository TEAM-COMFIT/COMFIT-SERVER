package sopt.comfit.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ReIssueTokenRequestDto(
        @NotBlank
        @Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String refreshToken
) {
}
