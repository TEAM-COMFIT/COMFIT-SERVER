package sopt.comfit.user.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import sopt.comfit.global.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum UserCompanyErrorCode implements ErrorCode {

    USER_COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_COMPANY_404_001", "해당하는 북마크 기업을 찾을 수 없습니다");

    private final HttpStatus status;
    private final String prefix;
    private final String message;
}
