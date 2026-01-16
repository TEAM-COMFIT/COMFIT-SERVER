package sopt.comfit.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import sopt.comfit.global.enums.EIndustry;

@Getter
@RequiredArgsConstructor
public enum EIndustryErrorCode implements ErrorCode{
    EINDUSTRY_NOT_FOUND(HttpStatus.NOT_FOUND, "EINDUSTRY_404_001", "존재하지 않는 산업군입니다");

    private final HttpStatus status;
    private final String prefix;
    private final String message;
}
