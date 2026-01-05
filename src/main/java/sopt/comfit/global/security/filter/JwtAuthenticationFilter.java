package sopt.comfit.global.security.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import sopt.comfit.global.constants.Constants;
import sopt.comfit.global.security.info.JwtAuthenticationToken;
import sopt.comfit.global.security.info.JwtUserInfo;
import sopt.comfit.global.security.manager.JwtAuthenticationManager;
import sopt.comfit.global.security.util.HeaderUtil;
import sopt.comfit.global.security.util.JwtUtil;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtAuthenticationManager jwtAuthenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        log.info(request.getHeader(Constants.PREFIX_AUTH));
        String token = HeaderUtil.refineHeader(request, Constants.PREFIX_AUTH, Constants.BEARER);

        Claims claim = jwtUtil.validateToken(token);
        log.info("claim: getUserId() = {}", claim.get(Constants.CLAIM_USER_ID, Long.class));

        JwtUserInfo jwtUserInfo = JwtUserInfo.from(claim);

        JwtAuthenticationToken unAuthenticatedToken = new JwtAuthenticationToken(jwtUserInfo);

        JwtAuthenticationToken authenticatedToken = (JwtAuthenticationToken) jwtAuthenticationManager.authenticate(unAuthenticatedToken);

        log.info("Authentication Successful: {}", authenticatedToken);

        authenticatedToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authenticatedToken);
        SecurityContextHolder.setContext(securityContext);

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        AntPathMatcher pathMatcher = new AntPathMatcher();
        return Constants.NO_NEED_AUTH.stream()
                .anyMatch(patter -> pathMatcher.match(patter, request.getRequestURI()));

    }
}
