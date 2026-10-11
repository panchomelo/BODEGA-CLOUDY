package com.universidad.msinventario.controller;

import com.universidad.msinventario.dto.InventarioRequestDTO;
import com.universidad.msinventario.dto.InventarioResponseDTO;
import com.universidad.msinventario.dto.InventarioStockDTO;
import com.universidad.msinventario.service.InventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    @GetMapping
    public List<InventarioResponseDTO> listarTodo() {
        return inventarioService.listarTodo();
    }

    // NOTA rutas: /{id:\d+} solo dígitos para no chocar con /stock y /stock/ajuste.
    // Spring prefiere la ruta exacta, pero el constraint evita que "stock"
    // se interprete como id y falle la conversión a Long.
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<InventarioResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(inventarioService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<InventarioResponseDTO> guardar(@Valid @RequestBody InventarioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventarioService.guardar(dto));
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<InventarioResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody InventarioRequestDTO dto) {
        return ResponseEntity.ok(inventarioService.actualizar(id, dto));
    }

    // PUT /inventario/stock?productoId=1&cantidad=10 (negativa = salida/venta)
    // Versión query params (compatibilidad).
    @PutMapping(value = "/stock", params = {"productoId", "cantidad"})
    public ResponseEntity<InventarioResponseDTO> actualizarStock(
            @RequestParam Long productoId,
            @RequestParam Integer cantidad) {
        return ResponseEntity.ok(inventarioService.actualizarStock(productoId, cantidad));
    }

    // PUT /inventario/stock/ajuste con body DTO (usado por ms-movimiento).
    // Todo viaje entre servicios va vía DTOs, no por query params.
    @PutMapping("/stock/ajuste")
    public ResponseEntity<InventarioResponseDTO> ajustarStock(
            @Valid @RequestBody InventarioStockDTO dto) {
        return ResponseEntity.ok(
                inventarioService.actualizarStock(dto.getProductoId(), dto.getCantidad()));
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        inventarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
