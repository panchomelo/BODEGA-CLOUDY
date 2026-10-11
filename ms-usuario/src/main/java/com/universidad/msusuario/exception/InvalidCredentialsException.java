package com.universidad.msusuario.exception;

// Credenciales inválidas en login → 401 Unauthorized (no 400).
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
