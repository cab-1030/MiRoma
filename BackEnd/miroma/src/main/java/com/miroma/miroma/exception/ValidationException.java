package com.miroma.miroma.exception;

/**
 * Excepción para errores de validación
 */
public class ValidationException extends CustomException {
    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR");
    }

    public ValidationException(String message, String internalMessage) {
        super(message, "VALIDATION_ERROR", internalMessage);
    }
}

