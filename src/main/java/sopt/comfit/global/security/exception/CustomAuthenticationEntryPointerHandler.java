package sopt.comfit.global.security.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import sopt.comfit.global.exception.CommonErrorCode;
import sopt.comfit.global.exception.ErrorCode;
import sopt.comfit.global.security.info.AuthenticationResponse;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPointerHandler implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        ErrorCode errorCode = (ErrorCode) request.getAttribute("errorCode");
        if (errorCode == null){
            AuthenticationResponse.makeFailureResponse(response, CommonErrorCode.INVALID_USER);
            return ;
        }
        AuthenticationResponse.makeFailureResponse(response, errorCode);
    }
}
