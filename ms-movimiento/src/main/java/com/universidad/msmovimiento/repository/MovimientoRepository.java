package com.universidad.msmovimiento.repository;

import com.universidad.msmovimiento.model.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findByProductoId(Long productoId);
}
