package com.miroma.miroma.exception;

/**
 * Excepción para errores de autorización
 */
public class UnauthorizedException extends CustomException {
    public UnauthorizedException(String message) {
        super(message, "UNAUTHORIZED");
    }

    public UnauthorizedException() {
        super("No tienes autorización para realizar esta acción", "UNAUTHORIZED");
    }
}

