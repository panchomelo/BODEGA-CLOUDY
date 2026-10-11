package com.universidad.msmovimiento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO interno para el PUT /inventario/stock/ajuste de ms-inventario.
// Todo viaje entre servicios va vía DTOs, no por query params.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventarioStockDTO {
    private Long productoId;
    private Integer cantidad;
}
