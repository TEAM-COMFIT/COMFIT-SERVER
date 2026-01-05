package sopt.comfit.global.security.info;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;

    // 미인증
    public JwtAuthenticationToken(JwtUserInfo jwtUserInfo) {
        super(null);
        this.principal = jwtUserInfo;
        setAuthenticated(false);
    }

    // 인증됨
    public JwtAuthenticationToken(UserPrincipal userPrincipal) {
        super(userPrincipal.getAuthorities());
        this.principal = userPrincipal;
        setAuthenticated(true);
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public Object getCredentials() {
        return null; // 필요 없음
    }

}
