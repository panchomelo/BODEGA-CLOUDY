package com.universidad.msinventario.service;

import com.universidad.msinventario.dto.InventarioRequestDTO;
import com.universidad.msinventario.dto.InventarioResponseDTO;
import com.universidad.msinventario.dto.ProductoDTO;
import com.universidad.msinventario.exception.InventarioNotFoundException;
import com.universidad.msinventario.model.Inventario;
import com.universidad.msinventario.repository.InventarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventarioService {

    private final InventarioRepository inventarioRepository;
    private final WebClient webClient; // Inyectado desde AppConfig

    // ═══════════════════════════════════════════════════
    // COMUNICACIÓN CON ms-producto VÍA WEBCLIENT (estilo híbrido unificado,
    // igual que ms-producto/validarCategoria).
    // onStatus mapea el HTTP, try/catch cubre caído/timeout.
    // ═══════════════════════════════════════════════════
    private void validarProducto(Long productoId) {
        log.info("Validando existencia de producto ID: {} en ms-producto", productoId);
        try {
            webClient.get()
                    .uri("/productos/{id}", productoId)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, response -> {
                        log.error("Error: Producto ID {} no existe en el sistema maestro", productoId);
                        return Mono.error(new RuntimeException("Producto no encontrado"));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, response -> {
                        log.error("Error remoto en ms-producto validando producto {}", productoId);
                        return Mono.error(new RuntimeException("Error en ms-producto al validar producto"));
                    })
                    .bodyToMono(ProductoDTO.class)
                    .block();
        } catch (RuntimeException e) {
            if (e.getMessage() != null
                    && (e.getMessage().contains("Producto no encontrado")
                        || e.getMessage().contains("Error en ms-producto"))) {
                throw e;
            }
            throw new RuntimeException("No se puede conectar con ms-producto: " + e.getMessage());
        }
    }

    private Inventario buscarInventario(Long id) {
        return inventarioRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Error: Inventario con ID {} no encontrado", id);
                    return new InventarioNotFoundException("Inventario no encontrado con ID: " + id);
                });
    }

    private InventarioResponseDTO mapToResponse(Inventario i) {
        return InventarioResponseDTO.builder()
                .id(i.getId())
                .productoId(i.getProductoId())
                .stock(i.getStock())
                .build();
    }

    /**
     * Actualiza el stock de un producto validando su existencia remota.
     * Si la cantidad es negativa, se interpreta como una salida (ej. venta).
     * FIX: ahora sí actualiza el existente en vez de crear uno nuevo.
     */
    @Transactional
    public InventarioResponseDTO actualizarStock(Long productoId, Integer cantidad) {
        log.info("Iniciando actualización de stock para producto ID: {} (Cantidad: {})", productoId, cantidad);

        // 1. Comunicación remota: validar producto en ms-producto
        validarProducto(productoId);

        // 2. Debe existir inventario previo (update real, no create)
        Inventario inventario = inventarioRepository.findByProductoId(productoId)
                .orElseThrow(() -> {
                    log.warn("Producto ID {} no tiene inventario registrado", productoId);
                    return new InventarioNotFoundException(
                            "No existe inventario para el productoId: " + productoId);
                });

        // 3. Regla de negocio: validar stock insuficiente
        int nuevoStock = inventario.getStock() + cantidad;
        if (nuevoStock < 0) {
            log.warn("Intento de rebaja fallido: Stock insuficiente para producto ID {}", productoId);
            throw new RuntimeException("Stock insuficiente para realizar el descuento");
        }

        inventario.setStock(nuevoStock);
        Inventario guardado = inventarioRepository.save(inventario);
        log.info("Actualización exitosa. Producto ID: {}, Nuevo Stock: {}", productoId, nuevoStock);
        return mapToResponse(guardado);
    }

    @Transactional(readOnly = true)
    public List<InventarioResponseDTO> listarTodo() {
        log.info("Consultando stock general de la bodega");
        return inventarioRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InventarioResponseDTO obtenerPorId(Long id) {
        log.info("Buscando inventario con ID: {}", id);
        return mapToResponse(buscarInventario(id));
    }

    @Transactional
    public InventarioResponseDTO guardar(InventarioRequestDTO dto) {
        // 1. Comunicación remota: validar producto en ms-producto
        validarProducto(dto.getProductoId());

        // 2. Regla simple: no permitir crear duplicados por productoId
        Optional<Inventario> existente = inventarioRepository.findByProductoId(dto.getProductoId());
        if (existente.isPresent()) {
            log.warn("Producto ID {} ya tiene inventario registrado", dto.getProductoId());
            throw new RuntimeException("productoId ya existe");
        }

        Inventario inventario = Inventario.builder()
                .productoId(dto.getProductoId())
                .stock(0)
                .build();

        // 3. Regla de negocio: validar stock insuficiente
        int nuevoStock = inventario.getStock() + dto.getStock();
        if (nuevoStock < 0) {
            log.warn("Intento de rebaja fallido: Stock insuficiente para producto ID {}", dto.getProductoId());
            throw new RuntimeException("Stock insuficiente para realizar el descuento");
        }

        inventario.setStock(nuevoStock);
        return mapToResponse(inventarioRepository.save(inventario));
    }

    @Transactional
    public InventarioResponseDTO actualizar(Long id, InventarioRequestDTO dto) {
        Inventario existente = buscarInventario(id);

        // 1. Comunicación remota: validar producto en ms-producto
        validarProducto(dto.getProductoId());

        // 2. Regla simple: no permitir cambio de productoId
        if (!dto.getProductoId().equals(existente.getProductoId())) {
            throw new RuntimeException("productoId no coincide con el inventario existente");
        }

        // 3. Regla de negocio: validar stock insuficiente
        int nuevoStock = existente.getStock() + dto.getStock();
        if (nuevoStock < 0) {
            log.warn("Intento de rebaja fallido: Stock insuficiente para producto ID {}", dto.getProductoId());
            throw new RuntimeException("Stock insuficiente para realizar el descuento");
        }

        existente.setStock(nuevoStock);
        return mapToResponse(inventarioRepository.save(existente));
    }

    @Transactional
    public void eliminar(Long id) {
        log.warn("Eliminando inventario ID: {}", id);
        Inventario existente = buscarInventario(id);
        inventarioRepository.delete(existente);
        log.info("Inventario ID: {} eliminado", id);
    }
}
