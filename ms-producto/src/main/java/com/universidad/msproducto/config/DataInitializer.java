package com.universidad.msproducto.config;

import com.universidad.msproducto.model.Producto;
import com.universidad.msproducto.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// ═══════════════════════════════════════════════════
// IMPORTANTE: Arrancar PRIMERO ms-categoria.
// Los categoriaId 1-5 deben existir en ese servicio
// ANTES de que este DataInitializer los use.
// ═══════════════════════════════════════════════════
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductoRepository repository;

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            log.info(">>> Productos ya cargados. Se omite inicialización.");
            return;
        }
        log.info(">>> Cargando productos iniciales...");
        repository.save(Producto.builder().nombre("Arroz Costeño 5kg").precio(28.50).categoriaId(1L).build());
        repository.save(Producto.builder().nombre("Coca Cola 2L").precio(12.00).categoriaId(2L).build());
        repository.save(Producto.builder().nombre("Leche Gloria 1L").precio(6.50).categoriaId(3L).build());
        repository.save(Producto.builder().nombre("Detergente Marsella 1kg").precio(15.90).categoriaId(4L).build());
        repository.save(Producto.builder().nombre("Galleta Oreo 6un").precio(4.50).categoriaId(5L).build());
        log.info(">>> 5 productos cargados OK.");
    }
}
