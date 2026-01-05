package sopt.comfit.global.dto;

public record JwtDto(
        String accessToken,

        String refreshToken
) {
    public static JwtDto of(String accessToken, String refreshToken) {
        return new JwtDto(accessToken, refreshToken);
    }
}
