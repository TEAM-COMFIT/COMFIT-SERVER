package sopt.comfit.global.constants;

import org.springframework.util.AntPathMatcher;

import java.util.List;

public class Constants {
    public static final String PREFIX_AUTH = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_USER_ROLE = "role";
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

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
            "/actuator/**"
    );
}
