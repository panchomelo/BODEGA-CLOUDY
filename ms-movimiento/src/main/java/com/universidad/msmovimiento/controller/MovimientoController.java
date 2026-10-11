package com.universidad.msmovimiento.controller;

import com.universidad.msmovimiento.dto.MovimientoRequestDTO;
import com.universidad.msmovimiento.dto.MovimientoResponseDTO;
import com.universidad.msmovimiento.service.MovimientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/movimientos")
@RequiredArgsConstructor
public class MovimientoController {

    private final MovimientoService movimientoService;

    @PostMapping
    public ResponseEntity<MovimientoResponseDTO> registrar(
            @Valid @RequestBody MovimientoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(movimientoService.registrarMovimiento(request));
    }

    @GetMapping
    public List<MovimientoResponseDTO> listarTodo() {
        return movimientoService.listarTodo();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovimientoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(movimientoService.obtenerPorId(id));
    }
}
