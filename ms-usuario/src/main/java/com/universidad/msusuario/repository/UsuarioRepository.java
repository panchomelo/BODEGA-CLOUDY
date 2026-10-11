package com.universidad.msusuario.repository;

import com.universidad.msusuario.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

// ═══════════════════════════════════════════════════
// JpaRepository<Usuario, Long> nos da gratis:
//   findAll(), findById(), save(), deleteById(), count()…
//
// @Query usa JPQL: se escribe sobre CLASES y ATRIBUTOS Java,
// NO sobre tablas y columnas SQL.
// ═══════════════════════════════════════════════════
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // LOWER() en ambos lados → búsqueda case-insensitive
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    List<Usuario> buscarPorUsername(@Param("username") String username);
}
