package com.universidad.mscategoria.dto;

import jakarta.validation.constraints.NotBlank;
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
public class CategoriaRequestDTO {

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String nombre;

    @Size(max = 255, message = "Máximo 255 caracteres")
    private String descripcion;
}
