package sopt.comfit.global.security.info;

import io.jsonwebtoken.Claims;
import sopt.comfit.global.constants.Constants;
import sopt.comfit.user.domain.ERole;

public record JwtUserInfo (
        Long userId,

        ERole role
){
    public static JwtUserInfo from(Claims claims){
        return new JwtUserInfo(
                claims.get(Constants.CLAIM_USER_ID, Long.class),
                ERole.valueOf(claims.get(Constants.CLAIM_USER_ROLE, String.class))
        );
    }
}
