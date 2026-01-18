package sopt.comfit.global.constants;

import org.springframework.util.AntPathMatcher;

import java.util.List;

public class Constants {
    public static final String PREFIX_AUTH = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_USER_ROLE = "role";
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    public static final String BASE_URL =
            "https://bucket-com-fit-server.s3.ap-northeast-2.amazonaws.com/";

    public static List<String> NO_NEED_AUTH = List.of(
            "/swagger",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/api-docs",
            "/api-docs/**",
            "/v3/api-docs/**",
            "/api/health",
            "/api/health-check",
            "/api/v1/login",
            "/api/v1/re-issued",
            "/actuator/**",
            "/api/v1/oauth/kakao/callback",
            "/favicon.ico",
            "/.well-known/**"
    );
}
