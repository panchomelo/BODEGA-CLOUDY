package com.universidad.msusuario.controller;

import com.universidad.msusuario.dto.AuthRequest;
import com.universidad.msusuario.dto.AuthResponse;
import com.universidad.msusuario.dto.RefreshRequest;
import com.universidad.msusuario.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Endpoints públicos de autenticación (ver SecurityConfig).
// Login NO crea usuarios: autentica uno existente y devuelve el par JWT.
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(usuarioService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(usuarioService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        usuarioService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}
