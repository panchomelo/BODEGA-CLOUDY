package com.universidad.msinventario.exception;

// Excepción de dominio: el inventario pedido no existe.
// GlobalExceptionHandler la convierte en 404 Not Found.
public class InventarioNotFoundException extends RuntimeException {
    public InventarioNotFoundException(String message) {
        super(message);
    }
}
