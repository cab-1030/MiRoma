package com.miroma.miroma.exception;

import com.miroma.miroma.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// Spring Security imports (comentados si no está disponible)
// import org.springframework.security.access.AccessDeniedException;
// import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.miroma.miroma.config.LoggingConfig;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones
 * Convierte todas las excepciones en respuestas de error seguras
 * que no revelan información sensible a atacantes
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja excepciones personalizadas del sistema
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(
            CustomException ex, HttpServletRequest request) {
        
        // Log interno con información completa (solo en servidor)
        logger.error("CustomException: {} - Path: {}", ex.getInternalMessage(), request.getRequestURI(), ex);
        
        // Enviar a cola de logging para procesamiento asíncrono
        LoggingConfig.logToQueue("ERROR", ex.getInternalMessage(), 
            "CustomException: " + request.getRequestURI(), ex);
        
        ErrorResponse error = new ErrorResponse(
            ex.getUserMessage(),
            ex.getErrorCode(),
            request.getRequestURI()
        );
        
        HttpStatus status = determineHttpStatus(ex);
        return ResponseEntity.status(status).body(error);
    }

    /**
     * Maneja excepciones de validación
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        logger.warn("Validation error: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        // Mensaje genérico para el usuario
        String message = "Los datos proporcionados no son válidos. Por favor, verifica los campos e intenta nuevamente.";
        
        ErrorResponse error = new ErrorResponse(message, "VALIDATION_ERROR", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja errores de tipo de argumento
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        logger.warn("Type mismatch: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        
        ErrorResponse error = new ErrorResponse(
            "El tipo de dato proporcionado no es válido",
            "INVALID_ARGUMENT_TYPE",
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja errores de base de datos (integridad referencial, etc.)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        
        // Log completo del error (solo en servidor)
        logger.error("Data integrity violation: {} - Path: {}", 
            ex.getMessage(), request.getRequestURI(), ex);
        
        // Mensaje genérico para el usuario
        String message = "No se pudo completar la operación debido a una restricción de datos. " +
                        "Por favor, verifica la información e intenta nuevamente.";
        
        ErrorResponse error = new ErrorResponse(message, "DATA_INTEGRITY_ERROR", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja errores SQL
     */
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ErrorResponse> handleSQLException(
            SQLException ex, HttpServletRequest request) {
        
        // Log completo del error (solo en servidor)
        logger.error("SQL error: {} - Path: {}", ex.getMessage(), request.getRequestURI(), ex);
        
        // Mensaje genérico para el usuario
        ErrorResponse error = new ErrorResponse(
            "Ocurrió un error al procesar la solicitud. Por favor, intenta nuevamente más tarde.",
            "DATABASE_ERROR",
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Maneja recursos no encontrados
     */
    @ExceptionHandler({ResourceNotFoundException.class, EmptyResultDataAccessException.class})
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            Exception ex, HttpServletRequest request) {
        
        logger.warn("Resource not found: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        
        String message = ex instanceof ResourceNotFoundException 
            ? ((ResourceNotFoundException) ex).getUserMessage()
            : "El recurso solicitado no fue encontrado";
        
        ErrorResponse error = new ErrorResponse(message, "RESOURCE_NOT_FOUND", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Maneja errores de autenticación de Spring Security
     */
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleSpringSecurityAuthentication(
            org.springframework.security.core.AuthenticationException ex, HttpServletRequest request) {
        
        logger.warn("Authentication failed: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        
        ErrorResponse error = new ErrorResponse(
            "Credenciales inválidas o sesión expirada",
            "AUTHENTICATION_ERROR",
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Maneja errores de autorización de Spring Security
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleSpringSecurityAccessDenied(
            org.springframework.security.access.AccessDeniedException ex, HttpServletRequest request) {
        
        logger.warn("Access denied: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        
        ErrorResponse error = new ErrorResponse(
            "No tienes permisos para acceder a este recurso",
            "ACCESS_DENIED",
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Maneja errores de autenticación
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            UnauthorizedException ex, HttpServletRequest request) {
        
        logger.warn("Unauthorized access attempt: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        
        ErrorResponse error = new ErrorResponse(
            ex.getUserMessage(),
            "UNAUTHORIZED",
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Maneja rutas no encontradas
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(
            NoHandlerFoundException ex, HttpServletRequest request) {
        
        logger.warn("No handler found: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        
        ErrorResponse error = new ErrorResponse(
            "La ruta solicitada no existe",
            "NOT_FOUND",
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Maneja todas las demás excepciones no manejadas
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        // Log completo del error (solo en servidor)
        logger.error("Unexpected error: {} - Path: {}", ex.getMessage(), request.getRequestURI(), ex);
        
        // Enviar a cola de logging para procesamiento asíncrono
        LoggingConfig.logToQueue("ERROR", ex.getMessage(), 
            "UnexpectedError: " + request.getRequestURI(), ex);
        
        // Mensaje genérico para el usuario (no revela detalles del error)
        ErrorResponse error = new ErrorResponse(
            "Ocurrió un error inesperado. Por favor, intenta nuevamente más tarde.",
            "INTERNAL_SERVER_ERROR",
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Determina el código HTTP apropiado basado en el tipo de excepción
     */
    private HttpStatus determineHttpStatus(CustomException ex) {
        if (ex instanceof ResourceNotFoundException) {
            return HttpStatus.NOT_FOUND;
        } else if (ex instanceof UnauthorizedException) {
            return HttpStatus.UNAUTHORIZED;
        } else if (ex instanceof ValidationException) {
            return HttpStatus.BAD_REQUEST;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}

