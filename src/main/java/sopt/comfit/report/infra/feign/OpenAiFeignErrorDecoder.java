package sopt.comfit.report.infra.feign;

import feign.Response;
import feign.codec.ErrorDecoder;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.report.exception.AIReportErrorCode;

public class OpenAiFeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String s, Response response) {
        return switch (response.status()){
            case 401 -> BaseException.type(AIReportErrorCode.AI_AUTH_FAILED);
            case 429 -> BaseException.type(AIReportErrorCode.AI_RATE_LIMITED);
            default -> {
                if(response.status() >= 500){
                    yield BaseException.type(AIReportErrorCode.AI_SERVER_ERROR);
                }
                yield new Default().decode(s, response);
            }
        };
    }
}
