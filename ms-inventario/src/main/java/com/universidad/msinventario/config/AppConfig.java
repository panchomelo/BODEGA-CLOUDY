package com.universidad.msinventario.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

// WebClient con baseUrl UNA SOLA VEZ aquí.
// En el Service solo se indica la ruta relativa: /productos/{id}
@Configuration
public class AppConfig {

    @Value("${ms.producto.url}")
    private String productoUrl;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(productoUrl)   // http://localhost:8083
                .build();
    }
}
