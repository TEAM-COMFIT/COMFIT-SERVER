package sopt.comfit.global.security.provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import sopt.comfit.global.security.info.JwtAuthenticationToken;
import sopt.comfit.global.security.info.JwtUserInfo;
import sopt.comfit.global.security.info.UserPrincipal;
import sopt.comfit.global.security.service.CustomUserDetailService;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final CustomUserDetailService customUserDetailService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.info("Authenticated User Process");
        return authOfAfterLogin((JwtUserInfo) authentication.getPrincipal());
    }

    private Authentication authOfAfterLogin(JwtUserInfo jwtUserInfo){
        UserPrincipal userPrincipal = customUserDetailService.loadUserById(jwtUserInfo.userId());
        return new JwtAuthenticationToken(userPrincipal);
    }
    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
