package com.miroma.miroma.exception;

/**
 * Excepción para recursos no encontrados
 */
public class ResourceNotFoundException extends CustomException {
    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(
            String.format("No se encontró el %s solicitado", resourceName),
            "RESOURCE_NOT_FOUND",
            String.format("Resource %s with identifier %s not found", resourceName, identifier)
        );
    }

    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND");
    }
}

