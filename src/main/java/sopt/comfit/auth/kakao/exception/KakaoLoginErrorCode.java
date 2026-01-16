package sopt.comfit.auth.kakao.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import sopt.comfit.global.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum KakaoLoginErrorCode implements ErrorCode {
    USERINFO_NOT_FOUND(HttpStatus.NOT_FOUND,"KAKAO_404_001" , "카카오 유저 정보를 찾을 수 없습니다." ),
    KAKAO_ACCESS_TOKEN_FAIL(HttpStatus.BAD_REQUEST, "KAKAO_400_001" ,"카카오 accessToken 발급에 실패했습니다." );

    private final HttpStatus status;
    private final String prefix;
    private final String message;
}
