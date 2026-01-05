package sopt.comfit.global.dto;

import sopt.comfit.global.exception.ErrorCode;

public record CustomErrorResponse(

        int status,

        String prefix,

        String message
) {

    public static CustomErrorResponse from(ErrorCode errorCode) {
        return new CustomErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getPrefix(),
                errorCode.getMessage());
    }

    public static CustomErrorResponse of(ErrorCode errorCode, String message) {
        return new CustomErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getPrefix(),
                message);
    }
}
