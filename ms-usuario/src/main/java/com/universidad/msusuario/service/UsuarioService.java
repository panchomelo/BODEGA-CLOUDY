package com.universidad.msusuario.service;

import com.universidad.msusuario.dto.UsuarioRequestDTO;
import com.universidad.msusuario.dto.UsuarioResponseDTO;
import com.universidad.msusuario.exception.UsuarioNotFoundException;
import com.universidad.msusuario.model.Usuario;
import com.universidad.msusuario.repository.UsuarioRepository;
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
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Transactional
    public UsuarioResponseDTO registrar(UsuarioRequestDTO request) {
        log.info("Registrando nuevo usuario: {}", request.getUsername());

        // Validación de existencia previa (integridad de datos)
        if (usuarioRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("El nombre de usuario ya está en uso");
        }
        if (request.getEmail() != null && usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está en uso");
        }

        Usuario usuario = Usuario.builder()
                .username(request.getUsername())
                .password(request.getPassword()) // Nota: en producción usar BCrypt
                .nombre(request.getNombre())
                .email(request.getEmail())
                .rol(request.getRol())
                .build();

        Usuario guardado = usuarioRepository.save(usuario);
        log.info("Usuario guardado exitosamente con ID: {}", guardado.getId());
        return mapToResponse(guardado);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarTodos() {
        log.info("Consultando lista completa de usuarios");
        return usuarioRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID: " + id));
        return mapToResponse(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> buscarPorUsername(String username) {
        return usuarioRepository.buscarPorUsername(username).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UsuarioResponseDTO actualizar(Long id, UsuarioRequestDTO request) {
        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID: " + id));
        if (!existente.getUsername().equals(request.getUsername())
                && usuarioRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("El nombre de usuario ya está en uso");
        }
        if (request.getEmail() != null && !request.getEmail().equals(existente.getEmail())
                && usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está en uso");
        }
        existente.setUsername(request.getUsername());
        existente.setPassword(request.getPassword());
        existente.setNombre(request.getNombre());
        existente.setEmail(request.getEmail());
        existente.setRol(request.getRol());
        Usuario actualizado = usuarioRepository.save(existente);
        log.info("Usuario ID: {} actualizado correctamente", id);
        return mapToResponse(actualizado);
    }

    @Transactional
    public void eliminar(Long id) {
        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuario no encontrado con ID: " + id));
        usuarioRepository.delete(existente);
        log.info("Usuario ID: {} eliminado", id);
    }

    // Método auxiliar de mapeo Entidad → ResponseDTO (nunca expone password)
    private UsuarioResponseDTO mapToResponse(Usuario u) {
        return UsuarioResponseDTO.builder()
                .id(u.getId())
                .username(u.getUsername())
                .nombre(u.getNombre())
                .email(u.getEmail())
                .rol(u.getRol())
                .build();
    }
}
