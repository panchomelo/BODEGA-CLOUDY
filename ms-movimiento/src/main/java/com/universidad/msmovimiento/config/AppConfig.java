package com.universidad.msmovimiento.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

// ═══════════════════════════════════════════════════
// Doble dependencia: un WebClient por destino, cada uno
// con su baseUrl. En el Service se inyectan con @Qualifier
// y solo se usa la ruta relativa. Todo viaje vía DTOs.
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
                .build();
    }

    @Bean("inventarioWebClient")
    public WebClient inventarioWebClient() {
        return WebClient.builder()
                .baseUrl(inventarioUrl) // http://localhost:8084
                .build();
    }
}
