package com.universidad.msmovimiento.service;

import com.universidad.msmovimiento.dto.InventarioStockDTO;
import com.universidad.msmovimiento.dto.MovimientoRequestDTO;
import com.universidad.msmovimiento.dto.MovimientoResponseDTO;
import com.universidad.msmovimiento.dto.ProductoDTO;
import com.universidad.msmovimiento.exception.MovimientoNotFoundException;
import com.universidad.msmovimiento.model.Movimiento;
import com.universidad.msmovimiento.repository.MovimientoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final WebClient productoWebClient;
    private final WebClient inventarioWebClient;

    public MovimientoService(
            MovimientoRepository movimientoRepository,
            @Qualifier("productoWebClient") WebClient productoWebClient,
            @Qualifier("inventarioWebClient") WebClient inventarioWebClient) {
        this.movimientoRepository = movimientoRepository;
        this.productoWebClient = productoWebClient;
        this.inventarioWebClient = inventarioWebClient;
    }

    // ── 1. VALIDAR PRODUCTO EN MS-PRODUCTO (híbrido unificado) ──
    private void validarProducto(Long productoId) {
        log.info("Validando existencia de producto ID: {} en ms-producto", productoId);
        try {
            productoWebClient.get()
                    .uri("/productos/{id}", productoId)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, response -> {
                        log.error("Error: Producto ID {} no existe", productoId);
                        return Mono.error(new RuntimeException("Producto no existe"));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, response -> {
                        log.error("Error remoto en ms-producto validando {}", productoId);
                        return Mono.error(new RuntimeException("Error en ms-producto al validar producto"));
                    })
                    .bodyToMono(ProductoDTO.class)
                    .block();
        } catch (RuntimeException e) {
            if (e.getMessage() != null
                    && (e.getMessage().contains("Producto no existe")
                        || e.getMessage().contains("Error en ms-producto"))) {
                throw e;
            }
            throw new RuntimeException("No se puede conectar con ms-producto: " + e.getMessage());
        }
    }

    // ── 2-3. ACTUALIZAR INVENTARIO EN MS-INVENTARIO (solo vía DTO) ──
    // Propaga el body del error: si inventario responde 400 con
    // {"error":"Stock insuficiente..."}, el cliente de movimiento ve
    // esa causa real en vez de un genérico.
    private void actualizarInventario(Long productoId, int ajuste) {
        log.info("Actualizando inventario de producto ID: {} (ajuste: {})", productoId, ajuste);
        InventarioStockDTO dto = InventarioStockDTO.builder()
                .productoId(productoId)
                .cantidad(ajuste)
                .build();
        try {
            inventarioWebClient.put()
                    .uri("/inventario/stock/ajuste")
                    .bodyValue(dto)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, response -> {
                        log.error("Error: sin inventario para producto {}", productoId);
                        return Mono.error(new RuntimeException("Error al actualizar inventario: sin stock registrado"));
                    })
                    .onStatus(status -> status.value() == 400, response ->
                            response.bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
                                    .defaultIfEmpty(Map.of("error", "Error al actualizar inventario"))
                                    .flatMap(map -> {
                                        String causa = map.getOrDefault("error", "Error al actualizar inventario");
                                        log.error("Inventario rechazó ajuste de producto {}: {}", productoId, causa);
                                        return Mono.error(new RuntimeException(causa));
                                    })
                    )
                    .onStatus(HttpStatusCode::isError, response -> {
                        log.error("Error al actualizar inventario de producto {}", productoId);
                        return Mono.error(new RuntimeException("Error al actualizar inventario"));
                    })
                    .bodyToMono(Void.class)
                    .block();
        } catch (RuntimeException e) {
            if (e.getMessage() != null
                    && (e.getMessage().contains("Error al actualizar inventario")
                        || e.getMessage().contains("Stock insuficiente")
                        || e.getMessage().contains("sin stock registrado")
                        || e.getMessage().contains("productoId"))) {
                throw e;
            }
            throw new RuntimeException("No se puede conectar con ms-inventario: " + e.getMessage());
        }
    }

    private Movimiento buscarMovimiento(Long id) {
        return movimientoRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Error: Movimiento con ID {} no encontrado", id);
                    return new MovimientoNotFoundException("Movimiento no encontrado con ID: " + id);
                });
    }

    private MovimientoResponseDTO mapToDTO(Movimiento m) {
        return MovimientoResponseDTO.builder()
                .id(m.getId())
                .productoId(m.getProductoId())
                .cantidad(m.getCantidad())
                .tipo(m.getTipo())
                .fecha(m.getFecha())
                .build();
    }

    @Transactional
    public MovimientoResponseDTO registrarMovimiento(MovimientoRequestDTO request) {
        log.info("Registrando movimiento: {} para producto {}",
                request.getTipo(), request.getProductoId());

        // 1. VALIDAR PRODUCTO EN MS-PRODUCTO
        validarProducto(request.getProductoId());

        // 2. CALCULAR AJUSTE DE STOCK (negativa = salida/venta)
        int ajuste = "SALIDA".equalsIgnoreCase(request.getTipo())
                ? -request.getCantidad()
                : request.getCantidad();

        // 3. ACTUALIZAR INVENTARIO EN MS-INVENTARIO (solo DTO)
        actualizarInventario(request.getProductoId(), ajuste);

        // 4. GUARDAR MOVIMIENTO EN BD LOCAL
        Movimiento movimiento = Movimiento.builder()
                .productoId(request.getProductoId())
                .cantidad(request.getCantidad())
                .tipo(request.getTipo().toUpperCase())
                .build();

        Movimiento guardado = movimientoRepository.save(movimiento);
        log.info("Movimiento guardado con ID: {}", guardado.getId());
        return mapToDTO(guardado);
    }

    @Transactional(readOnly = true)
    public List<MovimientoResponseDTO> listarTodo() {
        return movimientoRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public MovimientoResponseDTO obtenerPorId(Long id) {
        return mapToDTO(buscarMovimiento(id));
    }
}
