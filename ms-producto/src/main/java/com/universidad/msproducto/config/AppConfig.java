package com.universidad.msproducto.config;

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
// WebClient se configura con baseUrl UNA SOLA VEZ aquí.
// bearerRelay() reenvía el Bearer entrante a ms-categoria:
// sin esto, la validación remota daría 401 aunque el dato exista.
// ═══════════════════════════════════════════════════
@Configuration
public class AppConfig {

    @Value("${ms.categoria.url}")
    private String categoriaUrl;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(categoriaUrl)   // http://localhost:8081
                .filter(bearerRelay())
                .build();
    }

    // Lee el token crudo guardado como credentials por JwtAuthFilter
    // y lo adjunta a la llamada saliente (mismo hilo, .block()).
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
