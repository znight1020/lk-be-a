package leehs.course.global.web;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import leehs.course.global.exception.ApplicationException;
import leehs.course.global.web.exception.error.RequestUserIdError;

@Component
public class RequestUserIdArgumentResolver implements HandlerMethodArgumentResolver {

    public static final String USER_ID_HEADER = "X-User-Id";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RequestUserId.class)
            && isLongType(parameter.getParameterType());
    }

    private static boolean isLongType(Class<?> parameterType) {
        return Long.class.equals(parameterType) || long.class.equals(parameterType);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest, WebDataBinderFactory binderFactory
    ) {
        String userIdHeader = webRequest.getHeader(USER_ID_HEADER);

        if (!StringUtils.hasText(userIdHeader)) {
            RequestUserIdError error = RequestUserIdError.REQUEST_USER_ID_HEADER_MISSING;
            throw new ApplicationException(error.name(), error.getStatus(), error.message(USER_ID_HEADER));
        }

        return parseUserId(userIdHeader);
    }

    private static Long parseUserId(String userIdHeader) {
        try {
            return Long.valueOf(userIdHeader);
        } catch (NumberFormatException ex) {
            RequestUserIdError error = RequestUserIdError.REQUEST_USER_ID_HEADER_INVALID;
            throw new ApplicationException(error.name(), error.getStatus(), error.message(USER_ID_HEADER));
        }
    }
}
