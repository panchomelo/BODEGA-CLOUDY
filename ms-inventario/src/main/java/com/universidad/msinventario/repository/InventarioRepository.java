package com.universidad.msinventario.repository;

import com.universidad.msinventario.model.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    // Un producto tiene como máximo una fila de inventario.
    // SELECT * FROM inventarios WHERE producto_id = ?
    Optional<Inventario> findByProductoId(Long productoId);

    boolean existsByProductoId(Long productoId);
}
