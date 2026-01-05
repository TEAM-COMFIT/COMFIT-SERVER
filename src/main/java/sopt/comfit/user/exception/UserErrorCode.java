package sopt.comfit.user.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import sopt.comfit.global.exception.ErrorCode;

@RequiredArgsConstructor
@Getter
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "해당하는 유저를 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "USER_002", "비밀번호가 일치하지 않습니다."),;



    private final HttpStatus status;
    private final String prefix;
    private final String message;
}
