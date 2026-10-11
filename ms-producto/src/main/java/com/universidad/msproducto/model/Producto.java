package com.universidad.msproducto.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// ═══════════════════════════════════════════════════
// IMPORTANTE: No hay @ManyToOne hacia Categoria.
// La relación es LÓGICA, no referenciada por FK en BD.
// Cada microservicio tiene su propia BD aislada.
// La integridad se garantiza por código (llamada HTTP).
// ═══════════════════════════════════════════════════
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private Double precio;

    // Solo el ID de la categoría. NO hay FK en la BD.
    // ms-producto valida por HTTP que este ID exista en ms-categoria.
    @Column(name = "categoria_id", nullable = false)
    private Long categoriaId;
}
