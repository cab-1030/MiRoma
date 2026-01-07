package com.miroma.miroma.service;

import com.miroma.miroma.exception.ValidationException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;

@Service
public class PasswordService {

    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;
    private static final int MIN_PASSWORD_LENGTH = 12;
    private static final int MAX_PASSWORD_LENGTH = 128;
    
    // Patrón para validar contraseña: al menos una mayúscula, una minúscula y un carácter especial
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?]");

    /**
     * Valida que la contraseña cumpla con los requisitos de seguridad
     * @param password Contraseña a validar
     * @throws ValidationException Si la contraseña no cumple los requisitos
     */
    public void validatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            throw new ValidationException("La contraseña es requerida");
        }
        
        // Validar longitud mínima
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException(
                String.format("La contraseña debe tener al menos %d caracteres", MIN_PASSWORD_LENGTH)
            );
        }
        
        // Validar longitud máxima
        if (password.length() > MAX_PASSWORD_LENGTH) {
            throw new ValidationException(
                String.format("La contraseña no puede exceder %d caracteres", MAX_PASSWORD_LENGTH)
            );
        }
        
        // Validar que tenga al menos una mayúscula
        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            throw new ValidationException("La contraseña debe contener al menos una letra mayúscula");
        }
        
        // Validar que tenga al menos una minúscula
        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            throw new ValidationException("La contraseña debe contener al menos una letra minúscula");
        }
        
        // Validar que tenga al menos un carácter especial
        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            throw new ValidationException(
                "La contraseña debe contener al menos un carácter especial (!@#$%^&*()_+-=[]{}|;:,.<>?)"
            );
        }
    }

    /**
     * Genera un hash SHA-256 con salt para la contraseña
     * @param password Contraseña en texto plano
     * @return Hash con salt en formato base64
     */
    public String hashPassword(String password) {
        // Validar la contraseña antes de hashearla
        validatePasswordStrength(password);
        try {
            // Generar salt aleatorio
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            // Combinar password con salt
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            digest.update(salt);
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            // Combinar salt y hash
            byte[] combined = new byte[salt.length + hash.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hash, 0, combined, salt.length, hash.length);

            // Retornar en base64
            return Base64.getEncoder().encodeToString(combined);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al generar hash de contraseña", e);
        }
    }

    /**
     * Verifica si una contraseña coincide con el hash almacenado
     * @param password Contraseña en texto plano
     * @param hashedPassword Hash almacenado
     * @return true si la contraseña coincide
     */
    public boolean verifyPassword(String password, String hashedPassword) {
        try {
            // Decodificar el hash
            byte[] combined = Base64.getDecoder().decode(hashedPassword);
            
            // Extraer salt y hash
            byte[] salt = new byte[SALT_LENGTH];
            System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);
            
            byte[] storedHash = new byte[combined.length - SALT_LENGTH];
            System.arraycopy(combined, SALT_LENGTH, storedHash, 0, storedHash.length);

            // Calcular hash de la contraseña proporcionada
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            digest.update(salt);
            byte[] computedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            // Comparar hashes
            return MessageDigest.isEqual(storedHash, computedHash);
        } catch (Exception e) {
            return false;
        }
    }
}

