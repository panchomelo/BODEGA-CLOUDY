package com.universidad.msmovimiento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

// DTO de SALIDA: lo que el servidor devuelve al cliente.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoResponseDTO {
    private Long id;
    private Long productoId;
    private Integer cantidad;
    private String tipo;
    private LocalDateTime fecha;
}
