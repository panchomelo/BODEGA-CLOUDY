package com.universidad.msusuario.config;

import com.universidad.msusuario.model.Usuario;
import com.universidad.msusuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Seeds con BCrypt (rehash: las claves viejas en plano se reemplazan
// recreando el volumen con docker compose down -v).
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            log.info(">>> Usuarios ya cargados. Se omite inicialización.");
            return;
        }
        log.info(">>> Cargando usuarios iniciales...");
        repository.save(Usuario.builder()
                .username("admin").password(passwordEncoder.encode("admin123"))
                .nombre("Administrador").email("admin@bodega.com").build());
        repository.save(Usuario.builder()
                .username("bodeguero").password(passwordEncoder.encode("bod123"))
                .nombre("Juan Bodeguero").email("bodeguero@bodega.com").build());
        log.info(">>> 2 usuarios cargados OK.");
    }
}
