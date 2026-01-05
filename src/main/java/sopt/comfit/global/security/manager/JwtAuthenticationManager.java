package sopt.comfit.global.security.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import sopt.comfit.global.security.provider.JwtAuthenticationProvider;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationManager implements AuthenticationManager {

    private final JwtAuthenticationProvider jwtAuthenticationProvider;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.info("AuthenticationManger Initiate");
        return jwtAuthenticationProvider.authenticate(authentication);
    }
}
