package sopt.comfit.university.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import sopt.comfit.global.exception.ErrorCode;

@RequiredArgsConstructor
@Getter
public enum UniversityErrorCode implements ErrorCode {

    UNIVERSITY_NOT_FOUND(HttpStatus.NOT_FOUND, "UNIVERSITY_404_001", "해당하는 학교를 찾을 수 없습니다");

    private final HttpStatus status;
    private final String prefix;
    private final String message;
}