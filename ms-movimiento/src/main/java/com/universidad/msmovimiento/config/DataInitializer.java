package com.universidad.msmovimiento.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Sin seed: los movimientos dependen de producto+inventario reales.
// Crear por POST /movimientos con ms-producto e ms-inventario arriba.
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    @Override
    public void run(String... args) {
        log.info(">>> ms-movimiento listo. Sin datos iniciales (crear por API).");
    }
}
