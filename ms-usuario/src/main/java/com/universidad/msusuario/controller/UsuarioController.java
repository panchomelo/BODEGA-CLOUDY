package com.universidad.msusuario.controller;

import com.universidad.msusuario.dto.UsuarioRequestDTO;
import com.universidad.msusuario.dto.UsuarioResponseDTO;
import com.universidad.msusuario.service.UsuarioService;
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
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Slf4j
public class UsuarioController {

    private final UsuarioService usuarioService;

    // GET /usuarios → todos
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        log.info("Petición recibida para listar todos los usuarios");
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    // GET /usuarios/{id}
    // Si no existe, UsuarioNotFoundException → 404 vía GlobalExceptionHandler
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        log.info("Buscando usuario con ID: {}", id);
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    // GET /usuarios/buscar?username=adm → búsqueda parcial JPQL
    @GetMapping("/buscar")
    public List<UsuarioResponseDTO> buscarPorUsername(@RequestParam String username) {
        return usuarioService.buscarPorUsername(username);
    }

    // POST /usuarios → @Valid activa las validaciones del DTO
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> registrar(@Valid @RequestBody UsuarioRequestDTO request) {
        log.info("Iniciando registro de usuario: {}", request.getUsername());
        UsuarioResponseDTO creado = usuarioService.registrar(request);
        return new ResponseEntity<>(creado, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequestDTO request) {
        log.info("Actualizando usuario ID: {}", id);
        return ResponseEntity.ok(usuarioService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
