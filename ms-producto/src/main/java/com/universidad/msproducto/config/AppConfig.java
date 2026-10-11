package com.universidad.msproducto.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

// ═══════════════════════════════════════════════════
// WebClient se configura con baseUrl UNA SOLA VEZ aquí.
// En el Service solo se indica la ruta relativa.
// Ejemplo: .uri("/categorias/{id}", id)
//
// @Value inyecta el valor de ms.categoria.url
// definido en application.yml.
// ═══════════════════════════════════════════════════
@Configuration
public class AppConfig {

    @Value("${ms.categoria.url}")
    private String categoriaUrl;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(categoriaUrl)   // http://localhost:8081
                .build();
    }
}
