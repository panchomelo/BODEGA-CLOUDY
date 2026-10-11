package com.universidad.msusuario.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// ═══════════════════════════════════════════════════
// @Entity  → Hibernate registra esta clase como entidad JPA.
//            Crea/actualiza la tabla 'usuarios' al arrancar.
// @Table   → nombre explícito de la tabla en MySQL.
// @Data    → Lombok genera getters, setters, equals, toString.
// @Builder → permite Usuario.builder().username(...).build()
// ═══════════════════════════════════════════════════
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario {

    // @Id + IDENTITY → clave primaria AUTO_INCREMENT en MySQL
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El username no puede estar vacío")
    @Size(max = 50, message = "Máximo 50 caracteres")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    // Nota: en producción usar BCrypt. De momento texto plano (sin Security/JWT).
    @NotBlank(message = "El password no puede estar vacío")
    @Size(max = 255, message = "Máximo 255 caracteres")
    @Column(nullable = false)
    private String password;

    @Size(max = 100, message = "Máximo 100 caracteres")
    @Column(length = 100)
    private String nombre;

    @Email(message = "Email inválido")
    @Size(max = 100, message = "Máximo 100 caracteres")
    @Column(unique = true, length = 100)
    private String email;

    // Solo ADMIN o BODEGUERO
    @Pattern(regexp = "ADMIN|BODEGUERO", message = "Rol debe ser ADMIN o BODEGUERO")
    @Column(length = 20)
    private String rol;
}
