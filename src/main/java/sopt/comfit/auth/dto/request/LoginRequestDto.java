package sopt.comfit.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(

        @NotBlank
        @Email
        @Schema(example = "test@test.com")
        String email,

        @NotBlank
        @Schema(example = "password123")
        String password

) {
}
