package com.pm.todoservice.security;

import com.pm.todoservice.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.UUID;

@Component
public class AuthContextArgumentResolver implements HandlerMethodArgumentResolver {
    public static final String USER_ID_HEADER = "X-Auth-User-Id";
    public static final String USER_ROLE_HEADER = "X-Auth-User-Role";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return AuthContext.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            throw new UnauthorizedException("Missing authentication context");
        }

        String userIdHeader = request.getHeader(USER_ID_HEADER);
        String roleHeader = request.getHeader(USER_ROLE_HEADER);

        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new UnauthorizedException("Missing authentication header: " + USER_ID_HEADER);
        }
        if (roleHeader == null || roleHeader.isBlank()) {
            throw new UnauthorizedException("Missing authentication header: " + USER_ROLE_HEADER);
        }

        try {
            return new AuthContext(UUID.fromString(userIdHeader), roleHeader);
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException("Invalid authentication header: " + USER_ID_HEADER);
        }
    }
}
