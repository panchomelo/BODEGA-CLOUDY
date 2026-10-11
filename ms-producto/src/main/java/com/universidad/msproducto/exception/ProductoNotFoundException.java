package com.universidad.msproducto.exception;

// Excepción de dominio: el producto pedido no existe.
// GlobalExceptionHandler la convierte en 404 Not Found.
public class ProductoNotFoundException extends RuntimeException {
    public ProductoNotFoundException(String message) {
        super(message);
    }
}
