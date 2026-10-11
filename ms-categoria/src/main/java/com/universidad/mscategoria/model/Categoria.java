package com.universidad.mscategoria.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// ═══════════════════════════════════════════════════
// @Entity  → Hibernate registra esta clase como entidad JPA.
//            Crea/actualiza la tabla 'categorias' al arrancar.
// @Table   → nombre explícito de la tabla en MySQL.
// @Data    → Lombok genera getters, setters, equals, toString.
// @Builder → permite Categoria.builder().nombre(...).build()
// ═══════════════════════════════════════════════════
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categorias")
public class Categoria {

    // @Id + IDENTITY → clave primaria AUTO_INCREMENT en MySQL
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 100, message = "Máximo 100 caracteres")
    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Size(max = 255, message = "Máximo 255 caracteres")
    @Column(length = 255)
    private String descripcion;
}
