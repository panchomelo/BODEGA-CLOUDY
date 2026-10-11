package com.universidad.msusuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// ═══════════════════════════════════════════════════
// DTO de ENTRADA: lo que el cliente envía en el body.
// Las validaciones viven aquí, no en la entidad.
// ═══════════════════════════════════════════════════
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequestDTO {

    @NotBlank(message = "El username no puede estar vacío")
    @Size(max = 50, message = "Máximo 50 caracteres")
    private String username;

    @NotBlank(message = "El password no puede estar vacío")
    @Size(max = 255, message = "Máximo 255 caracteres")
    private String password;

    @Size(max = 100, message = "Máximo 100 caracteres")
    private String nombre;

    @Email(message = "Email inválido")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String email;

    @Pattern(regexp = "ADMIN|BODEGUERO", message = "Rol debe ser ADMIN o BODEGUERO")
    private String rol;
}
