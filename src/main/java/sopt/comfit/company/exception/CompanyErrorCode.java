package sopt.comfit.company.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import sopt.comfit.global.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum CompanyErrorCode implements ErrorCode {

    COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "COMPANY_404_001", "해당하는 회사를 찾을 수 없습니다");

    private final HttpStatus status;
    private final String prefix;
    private final String message;
}
