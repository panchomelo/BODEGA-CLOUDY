package com.universidad.msproducto.repository;

import com.universidad.msproducto.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Query Method estándar: Spring genera el SQL automáticamente
    // SELECT * FROM productos WHERE categoria_id = ?
    List<Producto> findByCategoriaId(Long categoriaId);

    // JPQL: búsqueda parcial de nombre, insensible a mayúsculas
    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Producto> buscarPorNombre(@Param("nombre") String nombre);
}
