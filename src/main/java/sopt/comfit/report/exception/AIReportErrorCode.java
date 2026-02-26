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
    AI_REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "AI_404_001", "AI 리포트를 찾을 수 없습니다"),
    AI_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AI_503_001", "AI 서비스를 현재 이용할 수 없습니다.(서킷브레이커)"),
    AI_CIRCUIT_OPEN(HttpStatus.SERVICE_UNAVAILABLE, "AI_503_002", "AI 서비스 일시 중단됨"),
    AI_AUTH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI_500_002", "AI 서비스 인증 실패"),
    AI_RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "AI_429_001", "AI 요청 한도 초과"),
    AI_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "AI_502_002", "AI 서버 오류"),
    AI_CALL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI_500_003", "AI 병렬 호출 실패"),
    AI_RESPONSE_REQUIRED_FIELD_OMIT(HttpStatus.INTERNAL_SERVER_ERROR, "AI_500_004", "AI 응답이 필수 응답값을 누락했습니다.")
    ;
    private final HttpStatus status;
    private final String prefix;
    private final String message;

}
