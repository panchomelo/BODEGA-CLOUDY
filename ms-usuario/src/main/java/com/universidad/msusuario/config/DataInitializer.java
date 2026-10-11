package com.universidad.msusuario.config;

import com.universidad.msusuario.model.Usuario;
import com.universidad.msusuario.repository.UsuarioRepository;
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

    private final UsuarioRepository repository;

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            log.info(">>> Usuarios ya cargados. Se omite inicialización.");
            return;
        }
        log.info(">>> Cargando usuarios iniciales...");
        repository.save(Usuario.builder()
                .username("admin").password("admin123")
                .nombre("Administrador").email("admin@bodega.com").rol("ADMIN").build());
        repository.save(Usuario.builder()
                .username("bodeguero").password("bod123")
                .nombre("Juan Bodeguero").email("bodeguero@bodega.com").rol("BODEGUERO").build());
        log.info(">>> 2 usuarios cargados OK.");
    }
}
