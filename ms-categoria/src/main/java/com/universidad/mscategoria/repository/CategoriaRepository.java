package com.universidad.mscategoria.repository;

import com.universidad.mscategoria.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

// ═══════════════════════════════════════════════════
// JpaRepository<Categoria, Long> nos da gratis:
//   findAll(), findById(), save(), deleteById(), count()…
//
// @Query usa JPQL: se escribe sobre CLASES y ATRIBUTOS Java,
// NO sobre tablas y columnas SQL.
// ═══════════════════════════════════════════════════
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    // LOWER() en ambos lados → búsqueda case-insensitive
    // CONCAT('%', :nombre, '%') → patrón LIKE dinámico
    @Query("SELECT c FROM Categoria c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Categoria> buscarPorNombre(@Param("nombre") String nombre);

    boolean existsByNombre(String nombre);
}
