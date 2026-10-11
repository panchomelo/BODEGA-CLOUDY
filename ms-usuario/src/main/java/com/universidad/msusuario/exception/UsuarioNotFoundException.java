package com.universidad.msusuario.exception;

// Excepción de dominio: el usuario pedido no existe.
// GlobalExceptionHandler la convierte en 404 Not Found.
public class UsuarioNotFoundException extends RuntimeException {
    public UsuarioNotFoundException(String message) {
        super(message);
    }
}
