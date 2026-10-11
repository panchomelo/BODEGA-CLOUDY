package com.universidad.msproducto.service;

import com.universidad.msproducto.dto.CategoriaDTO;
import com.universidad.msproducto.dto.ProductoRequestDTO;
import com.universidad.msproducto.dto.ProductoResponseDTO;
import com.universidad.msproducto.exception.ProductoNotFoundException;
import com.universidad.msproducto.model.Producto;
import com.universidad.msproducto.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository repository;
    private final WebClient webClient;

    // ── Mapeo Entidad → ResponseDTO ──────────────────
    private ProductoResponseDTO mapToResponse(Producto p) {
        return ProductoResponseDTO.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .precio(p.getPrecio())
                .categoriaId(p.getCategoriaId())
                .build();
    }

    private Producto buscarProducto(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> {
                    log.error("Error: Producto con ID {} no encontrado", id);
                    return new ProductoNotFoundException("Producto no encontrado con ID: " + id);
                });
    }

    // ═══════════════════════════════════════════════════
    // COMUNICACIÓN CON ms-categoria VÍA WEBCLIENT (estilo híbrido unificado)
    //
    // webClient.get()          → método HTTP GET
    // .uri("/categorias/{id}") → ruta relativa + variable (baseUrl en AppConfig)
    // .retrieve()              → ejecuta la petición
    // .onStatus(404)           → categoría inexistente → Mono.error dominio
    // .onStatus(5xx)           → error remoto → Mono.error genérico
    // .bodyToMono(CategoriaDTO.class)→ DTO tipado (unificado, no Object)
    // .block()                 → bloquea hasta tener respuesta (uso síncrono)
    // try/catch exterior       → caído/timeout → 400 conexión
    // ═══════════════════════════════════════════════════
    private void validarCategoria(Long categoriaId) {
        log.info("Validando existencia de categoría ID: {} en ms-categoria", categoriaId);
        try {
            webClient.get()
                    .uri("/categorias/{id}", categoriaId)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, response -> {
                        log.error("Error: La categoría {} no existe en el sistema", categoriaId);
                        return Mono.error(new RuntimeException("Categoría no encontrada en el maestro de categorías"));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, response -> {
                        log.error("Error remoto en ms-categoria validando categoría {}", categoriaId);
                        return Mono.error(new RuntimeException("Error en ms-categoria al validar categoría"));
                    })
                    .bodyToMono(CategoriaDTO.class)
                    .block();
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Categoría no encontrada")) {
                throw e;
            }
            throw new RuntimeException("No se puede conectar con ms-categoria: " + e.getMessage());
        }
    }

    @Transactional
    public ProductoResponseDTO crear(ProductoRequestDTO dto) {
        log.info("Iniciando la creación del producto: {}", dto.getNombre());

        validarCategoria(dto.getCategoriaId());

        Producto producto = Producto.builder()
                .nombre(dto.getNombre())
                .precio(dto.getPrecio())
                .categoriaId(dto.getCategoriaId())
                .build();

        Producto guardado = repository.save(producto);
        log.info("Producto guardado exitosamente con ID: {}", guardado.getId());

        return mapToResponse(guardado);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listar() {
        log.info("Recuperando lista completa de productos");
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponseDTO obtenerPorId(Long id) {
        log.info("Buscando producto con ID: {}", id);
        return mapToResponse(buscarProducto(id));
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> obtenerPorCategoria(Long categoriaId) {
        return repository.findByCategoriaId(categoriaId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> buscarPorNombre(String nombre) {
        return repository.buscarPorNombre(nombre).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public ProductoResponseDTO actualizar(Long id, ProductoRequestDTO dto) {
        log.info("Actualizando datos del producto ID: {}", id);
        Producto producto = buscarProducto(id);

        validarCategoria(dto.getCategoriaId());

        producto.setNombre(dto.getNombre());
        producto.setPrecio(dto.getPrecio());
        producto.setCategoriaId(dto.getCategoriaId());

        Producto actualizado = repository.save(producto);
        log.info("Producto ID: {} actualizado correctamente", id);

        return mapToResponse(actualizado);
    }

    @Transactional
    public void eliminar(Long id) {
        log.warn("Eliminando definitivamente el producto ID: {}", id);
        Producto producto = buscarProducto(id);
        repository.delete(producto);
        log.info("Producto ID: {} eliminado de la base de datos", id);
    }
}
