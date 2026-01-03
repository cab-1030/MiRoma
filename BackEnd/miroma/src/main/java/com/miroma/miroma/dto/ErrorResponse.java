package com.miroma.miroma.dto;

import java.time.LocalDateTime;

/**
 * DTO estándar para respuestas de error
 * Diseñado para no revelar información sensible a atacantes
 */
public class ErrorResponse {
    private String message;
    private String code;
    private LocalDateTime timestamp;
    private String path;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String message, String code) {
        this();
        this.message = message;
        this.code = code;
    }

    public ErrorResponse(String message, String code, String path) {
        this(message, code);
        this.path = path;
    }

    // Getters y Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}

