package sopt.comfit.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    // ===== 공통 에러 (4xx) =====
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "COMMON_400_001", "입력값 검증에 실패했습니다."),
    JSON_PARSE_ERROR(HttpStatus.BAD_REQUEST, "COMMON_400_002", "요청 형식이 잘못되었습니다."),
    MISSING_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "COMMON_400_003", "필수 파라미터가 누락되었습니다."),
    MISSING_PATH_VARIABLE(HttpStatus.BAD_REQUEST, "COMMON_400_004", "필수 경로 변수가 누락되었습니다."),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "COMMON_400_005", "잘못된 데이터 타입입니다."),
    NOT_FOUND_URI(HttpStatus.NOT_FOUND, "COMMON_404_001", "존재하지 않는 URI입니다."),
    NOT_SUPPORTED_METHOD_ERROR(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_405_001", "지원하지 않는 HTTP 메서드입니다."),
    NOT_SUPPORTED_MEDIA_TYPE_ERROR(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "COMMON_415_008", "지원하지 않는 미디어 타입입니다."),
    DATA_INTEGRITY_VIOLATION(HttpStatus.BAD_REQUEST, "COMMON_400_009", "데이터 정합성 오류입니다"),
    NOT_SUPPORTED_SORT_TYPE(HttpStatus.BAD_REQUEST, "COMMON_400_010", "지원하지 않는 정렬 타입입니다."),

    // ==== 인증/인가 에러 (4xx) ====
    // ==== 인증 에러 (4xx) ====
    ACCESS_DENIED(HttpStatus.FORBIDDEN,"AUTH_403_001","권한이 없는 유저의 접근입니다."),
    INVALID_HEADER_VALUE(HttpStatus.UNAUTHORIZED,"AUTH_401_001", "올바르지 않은 헤더값입니다." ),
    EXPIRED_TOKEN_ERROR(HttpStatus.UNAUTHORIZED, "AUTH_401_002", "만료된 토큰입니다."),
    TOKEN_MALFORMED_ERROR(HttpStatus.UNAUTHORIZED, "AUTH_401_003", "토큰이 올바르지 않습니다."),
    TOKEN_TYPE_ERROR(HttpStatus.UNAUTHORIZED, "AUTH_401_004", "토큰 타입이 일치하지 않거나 비어있습니다."),
    TOKEN_UNSUPPORTED_ERROR(HttpStatus.UNAUTHORIZED, "AUTH_401_005", "지원하지않는 토큰입니다."),
    TOKEN_UNKNOWN_ERROR(HttpStatus.UNAUTHORIZED, "AUTH_401_006", "알 수 없는 토큰입니다."),
    AUTHENTICATION_USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_401_007", "인증된 사용자 정보를 찾을 수 없습니다"),

    // ===== 서버 에러 (5xx) =====
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_500_001", "서버 내부 오류가 발생했습니다.");


    private final HttpStatus status;
    private final String prefix;
    private final String message;

}
