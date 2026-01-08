package sopt.comfit.report.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import sopt.comfit.global.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum AIReportErrorCode implements ErrorCode {

    AI_RESPONSE_EMPTY(HttpStatus.BAD_GATEWAY, "AI_502_001", "AI 응답이 비어있습니다"),
    AI_RESPONSE_PARSE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI_500_001", "AI 응답 파싱에 실패했습니다"),
    AI_REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "AI_404_001", "AI 리포트를 찾을 수 없습니다");


    private final HttpStatus status;
    private final String prefix;
    private final String message;

}
