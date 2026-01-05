package sopt.comfit.global.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import sopt.comfit.global.exception.ErrorCode;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class AuthenticationResponse {

    public static void makeFailureResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(errorCode.getStatus().value());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", errorCode.getStatus().value());
        body.put("prefix", errorCode.getPrefix());
        body.put("message", errorCode.getMessage());

        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
    }
}
