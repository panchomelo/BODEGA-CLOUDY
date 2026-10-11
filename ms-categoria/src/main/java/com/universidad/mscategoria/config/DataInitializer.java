package com.universidad.mscategoria.config;

import com.universidad.mscategoria.model.Categoria;
import com.universidad.mscategoria.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// ═══════════════════════════════════════════════════
// CommandLineRunner: Spring ejecuta run() UNA VEZ
// al terminar de levantar el contexto (tablas ya creadas).
//
// Verificamos count() > 0 para no duplicar datos
// si la app se reinicia con datos existentes.
// ═══════════════════════════════════════════════════
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoriaRepository repository;

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            log.info(">>> Categorías ya cargadas. Se omite inicialización.");
            return;
        }
        log.info(">>> Cargando categorías iniciales...");
        repository.save(Categoria.builder().nombre("Abarrotes").descripcion("Arroz, azúcar, aceite, conservas").build());
        repository.save(Categoria.builder().nombre("Bebidas").descripcion("Gaseosas, jugos, agua, cervezas").build());
        repository.save(Categoria.builder().nombre("Lácteos").descripcion("Leche, queso, yogurt, mantequilla").build());
        repository.save(Categoria.builder().nombre("Limpieza").descripcion("Detergentes, jabones, desinfectantes").build());
        repository.save(Categoria.builder().nombre("Snacks").descripcion("Galletas, papas fritas, chocolates").build());
        log.info(">>> 5 categorías cargadas OK.");
    }
}
