package com.universidad.msmovimiento.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

// ═══════════════════════════════════════════════════
// Doble dependencia: un WebClient por destino, cada uno
// con su baseUrl + relay del Bearer entrante. Sin relay,
// las validaciones remotas darían 401 aunque el dato exista.
// ═══════════════════════════════════════════════════
@Configuration
public class AppConfig {

    @Value("${ms.producto.url}")
    private String productoUrl;

    @Value("${ms.inventario.url}")
    private String inventarioUrl;

    @Bean("productoWebClient")
    public WebClient productoWebClient() {
        return WebClient.builder()
                .baseUrl(productoUrl)   // http://localhost:8083
                .filter(bearerRelay())
                .build();
    }

    @Bean("inventarioWebClient")
    public WebClient inventarioWebClient() {
        return WebClient.builder()
                .baseUrl(inventarioUrl) // http://localhost:8084
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
