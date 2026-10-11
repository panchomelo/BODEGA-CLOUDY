package com.universidad.msinventario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO de SALIDA: lo que el servidor devuelve al cliente.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventarioResponseDTO {
    private Long id;
    private Long productoId;
    private Integer stock;
}
