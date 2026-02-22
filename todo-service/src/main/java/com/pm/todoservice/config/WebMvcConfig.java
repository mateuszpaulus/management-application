package com.pm.todoservice.config;

import com.pm.todoservice.security.AuthContextArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final AuthContextArgumentResolver authContextArgumentResolver;

    public WebMvcConfig(AuthContextArgumentResolver authContextArgumentResolver) {
        this.authContextArgumentResolver = authContextArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authContextArgumentResolver);
    }
}
