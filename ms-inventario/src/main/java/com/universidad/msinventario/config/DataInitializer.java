package com.universidad.msinventario.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// ═══════════════════════════════════════════════════
// Sin seed inicial: el stock depende de IDs reales de
// ms-producto, que deben existir antes (arrancar ms-producto
// primero). Crear inventarios por POST /inventario.
// ═══════════════════════════════════════════════════
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    @Override
    public void run(String... args) {
        log.info(">>> ms-inventario listo. Sin datos iniciales (crear por API).");
    }
}
