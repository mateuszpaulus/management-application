package com.pm.apigateway.filter;

import com.pm.apigateway.dto.TokenValidationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;


@Component
public class JwtValidationGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtValidationGatewayFilterFactory.Config> {

    private static final String USER_EMAIL_HEADER = "X-Auth-User-Email";
    private static final String USER_ROLE_HEADER = "X-Auth-User-Role";
    private static final String USER_ID_HEADER = "X-Auth-User-Id";

    private final WebClient webClient;

    public JwtValidationGatewayFilterFactory(WebClient.Builder webClientBuilder, @Value("${auth.service.url}") String authServiceUrl) {
        super(Config.class);
        this.webClient = webClientBuilder.baseUrl(authServiceUrl).build();
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (token == null || !token.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String configuredRoles = config != null ? config.getRequiredRoles() : null;
            Set<String> requiredRoles = parseRoles(configuredRoles);

            return webClient.get()
                    .uri("/validate")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()
                    .bodyToMono(TokenValidationResponse.class)
                    .flatMap(validation -> {
                        if (validation.role() == null || validation.email() == null || validation.userId() == null) {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }

                        if (!requiredRoles.isEmpty() && !requiredRoles.contains(validation.role())) {
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            return exchange.getResponse().setComplete();
                        }

                        var request = exchange.getRequest().mutate()
                                .header(USER_EMAIL_HEADER, validation.email())
                                .header(USER_ROLE_HEADER, validation.role())
                                .header(USER_ID_HEADER, validation.userId())
                                .build();

                        return chain.filter(exchange.mutate().request(request).build());
                    })
                    .onErrorResume(WebClientResponseException.Unauthorized.class, e -> {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    })
                    .onErrorResume(WebClientResponseException.Forbidden.class, e -> {
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    })
                    .onErrorResume(WebClientResponseException.class, e -> {
                        exchange.getResponse().setStatusCode(HttpStatus.BAD_GATEWAY);
                        return exchange.getResponse().setComplete();
                    })
                    .onErrorResume(e -> {
                        exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                        return exchange.getResponse().setComplete();
                    });
        };
    }

    @Override
    public java.util.List<String> shortcutFieldOrder() {
        return java.util.List.of("requiredRoles");
    }

    private Set<String> parseRoles(String roles) {
        if (roles == null || roles.isBlank()) {
            return Set.of();
        }

        return Arrays.stream(roles.split(","))
                .map(String::trim)
                .filter(role -> !role.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }

    public static class Config {
        private final String requiredRoles;

        public Config(String requiredRoles) {
            this.requiredRoles = requiredRoles;
        }

        public String getRequiredRoles() {
            return requiredRoles;
        }
    }
}
