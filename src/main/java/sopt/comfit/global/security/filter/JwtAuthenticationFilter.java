package sopt.comfit.global.security.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import sopt.comfit.global.constants.Constants;
import sopt.comfit.global.exception.CommonErrorCode;
import sopt.comfit.global.logging.MdcUtils;
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
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return Constants.NO_NEED_AUTH.stream()
                .anyMatch(pattern -> Constants.PATH_MATCHER.match(pattern, request.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // traceId/spanId는 Micrometer Tracing이 MDC에 자동 주입
            // MdcUtils.generateTraceId() 제거 — 커스텀 8자 UUID가 Micrometer traceId(32자 hex)를 덮어쓰는 충돌 방지

            String header = request.getHeader(Constants.PREFIX_AUTH);
            log.info("header:{}", header);

            if (header == null || !header.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = HeaderUtil.refineHeader(request, Constants.PREFIX_AUTH, Constants.BEARER);
            Claims claim = jwtUtil.validateToken(token);
            log.info("claim: getUserId() = {}", claim.get(Constants.CLAIM_USER_ID, Long.class));

            JwtUserInfo jwtUserInfo = JwtUserInfo.from(claim);
            MdcUtils.setUserId(jwtUserInfo.userId());

            JwtAuthenticationToken unAuthenticatedToken = new JwtAuthenticationToken(jwtUserInfo);
            JwtAuthenticationToken authenticatedToken = (JwtAuthenticationToken) jwtAuthenticationManager.authenticate(unAuthenticatedToken);
            log.info("Authentication Successful: {}", authenticatedToken);

            authenticatedToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authenticatedToken);
            SecurityContextHolder.setContext(securityContext);

            filterChain.doFilter(request, response);
        } catch (SecurityException e) {
            log.error("SecurityException: {}", e.getMessage());
            request.setAttribute("errorCode", CommonErrorCode.ACCESS_DENIED);
            filterChain.doFilter(request, response);
        } catch (MalformedJwtException e) {
            log.error("MalformedJwtException: {}", e.getMessage());
            request.setAttribute("errorCode", CommonErrorCode.TOKEN_MALFORMED_ERROR);
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            log.error("ExpiredJwtException: {}", e.getMessage());
            request.setAttribute("errorCode", CommonErrorCode.EXPIRED_TOKEN_ERROR);
            filterChain.doFilter(request, response);
        } catch (UnsupportedJwtException e) {
            log.error("UnsupportedJwtException: {}", e.getMessage());
            request.setAttribute("errorCode", CommonErrorCode.TOKEN_UNSUPPORTED_ERROR);
            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            log.error("JwtException: {}", e.getMessage());
            request.setAttribute("errorCode", CommonErrorCode.TOKEN_UNKNOWN_ERROR);
            filterChain.doFilter(request, response);
        } catch (IllegalArgumentException e) {
            log.error("IllegalArgumentException: {}", e.getMessage());
            request.setAttribute("errorCode", CommonErrorCode.TOKEN_TYPE_ERROR);
            filterChain.doFilter(request, response);
        } catch (UsernameNotFoundException e) {
            log.error("UsernameNotFoundException: {}", e.getMessage());
            request.setAttribute("errorCode", CommonErrorCode.AUTHENTICATION_USER_NOT_FOUND);
            filterChain.doFilter(request, response);
        } catch (AuthenticationCredentialsNotFoundException e) {
            log.error("AuthenticationCredentialsNotFoundException: {}", e.getMessage());
            request.setAttribute("errorCode", CommonErrorCode.INVALID_HEADER_VALUE);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Unexpected filter exception: {}", e.getMessage());
            request.setAttribute("errorCode", CommonErrorCode.INTERNAL_SERVER_ERROR);
            filterChain.doFilter(request, response);
        } finally {
            MdcUtils.clear();
        }
    }
}