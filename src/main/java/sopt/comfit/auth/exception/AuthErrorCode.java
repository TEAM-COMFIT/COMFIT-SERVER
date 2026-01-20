package sopt.comfit.auth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import sopt.comfit.global.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    USERINFO_NOT_FOUND(HttpStatus.NOT_FOUND,"KAKAO_404_001" , "카카오 유저 정보를 찾을 수 없습니다." ),
    KAKAO_ACCESS_TOKEN_FAIL(HttpStatus.BAD_REQUEST, "KAKAO_400_001" ,"카카오 accessToken 발급에 실패했습니다." ),
    REFRESH_TOKEN_EXPIRATION(HttpStatus.UNAUTHORIZED,"REFRESH_TOKEN_401_001" ,"만료된 토큰입니다." );

    private final HttpStatus status;
    private final String prefix;
    private final String message;
}
