package com.universidad.msinventario.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

// WebClient con baseUrl UNA SOLA VEZ aquí + relay del Bearer
// entrante hacia ms-producto (sin relay daría 401 interno).
@Configuration
public class AppConfig {

    @Value("${ms.producto.url}")
    private String productoUrl;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(productoUrl)   // http://localhost:8083
                .filter(bearerRelay())
                .build();
    }

    private ExchangeFilterFunction bearerRelay() {
        return (request, next) -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getCredentials() instanceof String token && !token.isBlank()) {
                request = ClientRequest.from(request)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build();
            }
            return next.exchange(request);
        };
    }
}
