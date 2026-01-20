package sopt.comfit.global.security.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.util.StringUtils;

public class HeaderUtil {
    public static String refineHeader(
            HttpServletRequest request,
            String headerName,
            String prefix
    ){
        String headerValue = request.getHeader(headerName);
        if(!StringUtils.hasText(headerValue) || !headerValue.startsWith(prefix))
            throw new AuthenticationCredentialsNotFoundException("올바르지 않은 헤더값 입니다.");

        return headerValue.substring(prefix.length());
    }
}