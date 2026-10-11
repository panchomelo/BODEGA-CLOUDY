package com.universidad.msinventario.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// ═══════════════════════════════════════════════════
// IMPORTANTE: No hay @ManyToOne hacia Producto.
// La relación es LÓGICA, no referenciada por FK en BD.
// Cada microservicio tiene su propia BD aislada.
// La integridad se garantiza por código (llamada HTTP).
// ═══════════════════════════════════════════════════
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "inventarios")
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Solo el ID del producto. NO hay FK en la BD.
    // ms-inventario valida por HTTP que este ID exista en ms-producto.
    @Column(name = "producto_id", nullable = false, unique = true)
    private Long productoId;

    @Column(nullable = false)
    private Integer stock;
}
