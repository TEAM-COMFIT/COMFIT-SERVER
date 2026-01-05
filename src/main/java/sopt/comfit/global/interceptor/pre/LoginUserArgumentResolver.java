package sopt.comfit.global.interceptor.pre;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import sopt.comfit.global.annotation.LoginUser;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.global.exception.CommonErrorCode;

@Component
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginUser.class)
                && parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        final Object userId = webRequest.getAttribute("USER_ID", NativeWebRequest.SCOPE_REQUEST);
        if(userId == null) {
            throw BaseException.type(CommonErrorCode.INVALID_HEADER_VALUE);
        }
        return userId;
    }
}
