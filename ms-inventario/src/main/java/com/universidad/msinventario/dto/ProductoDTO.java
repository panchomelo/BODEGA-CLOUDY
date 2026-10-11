package com.universidad.msinventario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO interno solo para deserializar el GET a ms-producto
// en validarProducto(). No se expone en la API de inventario.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDTO {
    private Long id;
    private String nombre;
    private Double precio;
    private Long categoriaId;
}
