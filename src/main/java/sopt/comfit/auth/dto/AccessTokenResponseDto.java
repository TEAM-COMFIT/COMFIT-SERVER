package sopt.comfit.auth.dto;

public record AccessTokenResponseDto(
        String accessToken
) {
    public static AccessTokenResponseDto from(String accessToken) {
        return new AccessTokenResponseDto(accessToken);
    }
}
