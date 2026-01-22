package sopt.comfit.global.security.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import sopt.comfit.global.exception.CommonErrorCode;
import sopt.comfit.global.exception.ErrorCode;

import java.io.IOException;

@Slf4j
@Component
public class CustomAuthenticationEntryPointerHandler implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        log.error("AuthenticationEntryPoint 호출 - URI: {}, Exception: {}",
                request.getRequestURI(),
                authException.getMessage(),
                authException);

        ErrorCode errorCode = (ErrorCode) request.getAttribute("errorCode");
        if (errorCode == null){
            AuthenticationResponse.makeFailureResponse(response, CommonErrorCode.ACCESS_DENIED);
            return ;
        }
        AuthenticationResponse.makeFailureResponse(response, errorCode);
    }
}
