package com.universidad.msusuario.service;

import com.universidad.msusuario.dto.AuthRequest;
import com.universidad.msusuario.dto.AuthResponse;
import com.universidad.msusuario.dto.UsuarioRequestDTO;
import com.universidad.msusuario.dto.UsuarioResponseDTO;
import com.universidad.msusuario.exception.InvalidCredentialsException;
import com.universidad.msusuario.exception.UsuarioNotFoundException;
import com.universidad.msusuario.model.RefreshToken;
import com.universidad.msusuario.model.Usuario;
import com.universidad.msusuario.repository.RefreshTokenRepository;
import com.universidad.msusuario.repository.UsuarioRepository;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

// @RequiredArgsConstructor → inyección por constructor de todos los final.
// Passwords siempre BCrypt: registro, update y seeds (ver DataInitializer).
@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // SHA-256 hex para guardar refresh sin exponerlo en claro.
    static String sha256(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo hashear el token");
        }
    }

    // ── AUTH ─────────────────────────────────────────
    @Transactional
    public AuthResponse login(AuthRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas"));
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            log.warn("Login fallido para username: {}", request.getUsername());
            throw new InvalidCredentialsException("Credenciales inválidas");
        }
        String access = jwtService.generateAccess(usuario.getUsername());
        String refresh = jwtService.generateRefresh(usuario.getUsername());
        refreshTokenRepository.save(RefreshToken.builder()
                .username(usuario.getUsername())
                .tokenHash(sha256(refresh))
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshExpiration()))
                .revoked(false)
                .build());
        log.info("Login exitoso: {}", usuario.getUsername());
        return AuthResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .tokenType("Bearer")
                .build();
    }

    // Rota el refresh: revoca el usado y emite un par nuevo.
    @Transactional
    public AuthResponse refresh(String refreshToken) {
        String username;
        try {
            username = jwtService.validateAndGetUsername(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidCredentialsException("Refresh token inválido o expirado");
        }
        RefreshToken stored = refreshTokenRepository.findByTokenHash(sha256(refreshToken))
                .orElseThrow(() -> new InvalidCredentialsException("Refresh token inválido o expirado"));
        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidCredentialsException("Refresh token inválido o expirado");
        }
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);
        String access = jwtService.generateAccess(username);
        String refresh = jwtService.generateRefresh(username);
        refreshTokenRepository.save(RefreshToken.builder()
                .username(username)
                .tokenHash(sha256(refresh))
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshExpiration()))
                .revoked(false)
                .build());
        return AuthResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .tokenType("Bearer")
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByTokenHash(sha256(refreshToken))
                .ifPresent(rt -> {
                    rt.setRevoked(true);
                    refreshTokenRepository.save(rt);
                });
        log.info("Logout: refresh revocado");
    }

    // ── CRUD (registro público libre) ────────────────
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
                .password(passwordEncoder.encode(request.getPassword()))
                .nombre(request.getNombre())
                .email(request.getEmail())
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
        existente.setPassword(passwordEncoder.encode(request.getPassword()));
        existente.setNombre(request.getNombre());
        existente.setEmail(request.getEmail());
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
                .build();
    }
}
