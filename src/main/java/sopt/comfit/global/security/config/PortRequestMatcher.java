package sopt.comfit.global.security.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.util.matcher.RequestMatcher;

public record PortRequestMatcher(int port) implements RequestMatcher {

    @Override
    public boolean matches(HttpServletRequest request) {
        return request.getLocalPort() == port;
    }
}
