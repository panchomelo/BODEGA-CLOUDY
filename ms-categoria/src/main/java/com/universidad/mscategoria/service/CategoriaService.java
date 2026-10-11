package com.universidad.mscategoria.service;

import com.universidad.mscategoria.dto.CategoriaRequestDTO;
import com.universidad.mscategoria.dto.CategoriaResponseDTO;
import com.universidad.mscategoria.exception.CategoriaNotFoundException;
import com.universidad.mscategoria.model.Categoria;
import com.universidad.mscategoria.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

// @RequiredArgsConstructor → inyección por constructor de todos los final.
// @Service → marca esta clase como componente de capa de negocio.
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> obtenerTodas() {
        return categoriaRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoriaResponseDTO obtenerPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNotFoundException("Categoría no encontrada con ID: " + id));
        return mapToResponse(categoria);
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> buscarPorNombre(String nombre) {
        return categoriaRepository.buscarPorNombre(nombre).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoriaResponseDTO crear(CategoriaRequestDTO request) {
        Categoria categoria = Categoria.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .build();

        Categoria guardada = categoriaRepository.save(categoria);
        log.info("Categoría guardada exitosamente con ID: {}", guardada.getId());
        return mapToResponse(guardada);
    }

    @Transactional
    public CategoriaResponseDTO actualizar(Long id, CategoriaRequestDTO request) {
        Categoria existente = categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNotFoundException("Categoría no encontrada con ID: " + id));
        if (!existente.getNombre().equalsIgnoreCase(request.getNombre())
                && categoriaRepository.existsByNombre(request.getNombre())) {
            throw new RuntimeException("El nombre de categoría ya está en uso");
        }
        existente.setNombre(request.getNombre());
        existente.setDescripcion(request.getDescripcion());
        Categoria actualizada = categoriaRepository.save(existente);
        log.info("Categoría ID: {} actualizada correctamente", id);
        return mapToResponse(actualizada);
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria existente = categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNotFoundException("Categoría no encontrada con ID: " + id));
        categoriaRepository.delete(existente);
        log.info("Categoría ID: {} eliminada", id);
    }

    // Método auxiliar de mapeo Entidad → ResponseDTO
    private CategoriaResponseDTO mapToResponse(Categoria categoria) {
        return CategoriaResponseDTO.builder()
                .id(categoria.getId())
                .nombre(categoria.getNombre())
                .descripcion(categoria.getDescripcion())
                .build();
    }
}
