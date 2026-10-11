package com.universidad.msmovimiento.exception;

// Excepción de dominio: el movimiento pedido no existe.
// GlobalExceptionHandler la convierte en 404 Not Found.
public class MovimientoNotFoundException extends RuntimeException {
    public MovimientoNotFoundException(String message) {
        super(message);
    }
}
