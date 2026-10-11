package com.universidad.msusuario.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// ═══════════════════════════════════════════════════
// Sin roles: todo usuario autenticado puede todo.
// El password se guarda hasheado con BCrypt (ver UsuarioService).
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
}
