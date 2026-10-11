package com.universidad.mscategoria.exception;

// Excepción de dominio: la categoría pedida no existe.
// GlobalExceptionHandler la convierte en 404 Not Found.
public class CategoriaNotFoundException extends RuntimeException {
    public CategoriaNotFoundException(String message) {
        super(message);
    }
}
