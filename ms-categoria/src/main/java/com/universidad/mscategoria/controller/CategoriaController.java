package com.universidad.mscategoria.controller;

import com.universidad.mscategoria.dto.CategoriaRequestDTO;
import com.universidad.mscategoria.dto.CategoriaResponseDTO;
import com.universidad.mscategoria.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

// @RestController = @Controller + @ResponseBody
// @RequestMapping → prefijo base para todos los endpoints
@RestController
@RequestMapping("/categorias")
@RequiredArgsConstructor
@Slf4j
public class CategoriaController {

    private final CategoriaService categoriaService;

    // GET /categorias → todas
    @GetMapping
    public ResponseEntity<List<CategoriaResponseDTO>> listarTodas() {
        log.info("Petición recibida para listar todas las categorías");
        return ResponseEntity.ok(categoriaService.obtenerTodas());
    }

    // GET /categorias/{id}
    // Si no existe, CategoriaNotFoundException → 404 vía GlobalExceptionHandler
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> buscarPorId(@PathVariable Long id) {
        log.info("Buscando categoría con ID: {}", id);
        return ResponseEntity.ok(categoriaService.obtenerPorId(id));
    }

    // GET /categorias/buscar?nombre=barr → búsqueda parcial JPQL
    @GetMapping("/buscar")
    public List<CategoriaResponseDTO> buscarPorNombre(@RequestParam String nombre) {
        return categoriaService.buscarPorNombre(nombre);
    }

    // POST /categorias → @Valid activa las validaciones del DTO
    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> guardar(@Valid @RequestBody CategoriaRequestDTO request) {
        log.info("Iniciando creación de categoría: {}", request.getNombre());
        CategoriaResponseDTO creada = categoriaService.crear(request);
        return new ResponseEntity<>(creada, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequestDTO request) {
        log.info("Actualizando categoría ID: {}", id);
        return ResponseEntity.ok(categoriaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
