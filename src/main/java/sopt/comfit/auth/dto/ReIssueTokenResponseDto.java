package sopt.comfit.auth.dto;

public record ReIssueTokenResponseDto(
        String accessToken
) {
    public static ReIssueTokenResponseDto from(String accessToken) {
        return new ReIssueTokenResponseDto(accessToken);
    }
}
