package com.universidad.msinventario.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO para ajuste de stock vía body (usado por ms-movimiento).
// cantidad positiva = entrada, negativa = salida.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventarioStockDTO {

    @NotNull(message = "El productoId es obligatorio")
    private Long productoId;

    @NotNull(message = "La cantidad es obligatoria")
    private Integer cantidad;
}
