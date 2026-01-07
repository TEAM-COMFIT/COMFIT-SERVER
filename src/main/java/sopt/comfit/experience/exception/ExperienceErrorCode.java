package sopt.comfit.experience.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import sopt.comfit.global.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum ExperienceErrorCode implements ErrorCode {

    END_DATE_BEFORE_START_DATE(HttpStatus.BAD_REQUEST, "EXPERIENCE_400_001", "종료일은 시작일보다 이후여야 합니다"),
    NOT_ALLOWED_FUTURE_DATE(HttpStatus.BAD_REQUEST, "EXPERIENCE_400_002", "미래 날짜는 입력할 수 없습니다"),
    NOT_FOUND_EXPERIENCE(HttpStatus.NOT_FOUND, "EXPERIENCE_404_001", "해당하는 경험을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String prefix;
    private final String message;
}
