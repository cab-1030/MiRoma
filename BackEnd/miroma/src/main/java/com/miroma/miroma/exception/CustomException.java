package com.miroma.miroma.exception;

/**
 * Excepción base personalizada para el sistema
 * Permite definir mensajes seguros para usuarios y códigos de error
 */
public class CustomException extends RuntimeException {
    private final String errorCode;
    private final String userMessage;
    private final String internalMessage; // Para logs internos

    public CustomException(String userMessage, String errorCode) {
        super(userMessage);
        this.userMessage = userMessage;
        this.errorCode = errorCode;
        this.internalMessage = userMessage;
    }

    public CustomException(String userMessage, String errorCode, String internalMessage) {
        super(internalMessage);
        this.userMessage = userMessage;
        this.errorCode = errorCode;
        this.internalMessage = internalMessage;
    }

    public CustomException(String userMessage, String errorCode, Throwable cause) {
        super(userMessage, cause);
        this.userMessage = userMessage;
        this.errorCode = errorCode;
        this.internalMessage = userMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public String getInternalMessage() {
        return internalMessage;
    }
}

