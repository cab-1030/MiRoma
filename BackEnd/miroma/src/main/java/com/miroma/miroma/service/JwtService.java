package com.miroma.miroma.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret:MiRomaSecretKey2025!ThisIsAVeryLongSecretKeyForJWTTokenGenerationAndValidation}")
    private String secret;

    @Value("${jwt.expiration:3600000}") // 1 hora por defecto
    private Long expiration;

    @Value("${jwt.refresh-expiration:604800000}") // 7 días por defecto
    private Long refreshExpiration;

    // Algoritmo permitido (solo HMAC-SHA256)
    private static final String ALLOWED_ALGORITHM = "HS256";
    
    // Longitud mínima de la clave secreta (256 bits = 32 bytes)
    private static final int MIN_SECRET_KEY_LENGTH = 32;

    /**
     * Valida que la clave secreta tenga la longitud mínima requerida
     * @throws IllegalArgumentException si la clave es demasiado corta
     */
    private void validateSecretKey() {
        if (secret == null || secret.length() < MIN_SECRET_KEY_LENGTH) {
            throw new IllegalArgumentException(
                String.format("La clave secreta JWT debe tener al menos %d caracteres (256 bits) para ser segura", 
                    MIN_SECRET_KEY_LENGTH)
            );
        }
    }

    /**
     * Obtiene la clave de firma HMAC-SHA256
     * Valida que la clave tenga la longitud mínima requerida
     * Fuerza HS256 usando una clave de exactamente 256 bits (32 bytes)
     */
    private SecretKey getSigningKey() {
        validateSecretKey();
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        // Asegurar que la clave tenga exactamente 32 bytes (256 bits) para HS256
        // Si es más larga, truncar; si es más corta, pad con ceros (aunque validateSecretKey ya valida esto)
        byte[] key256 = new byte[32];
        int copyLength = Math.min(keyBytes.length, 32);
        System.arraycopy(keyBytes, 0, key256, 0, copyLength);
        // Si la clave original es más corta, el resto ya está en ceros
        return Keys.hmacShaKeyFor(key256);
    }

    public String generateToken(Integer userId, String email, String nombre) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("nombre", nombre);
        claims.put("type", "access");
        
        return createToken(claims, email, expiration);
    }

    public String generateToken(Integer userId, String email, String nombre, Integer tokenVersion) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("nombre", nombre);
        claims.put("type", "access");
        claims.put("tokenVersion", tokenVersion);
        
        return createToken(claims, email, expiration);
    }

    public String generateRefreshToken(Integer userId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("type", "refresh");
        
        return createToken(claims, email, refreshExpiration);
    }

    private String createToken(Map<String, Object> claims, String subject, Long expirationTime) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        // Forzar el algoritmo HS256 explícitamente
        // getSigningKey() ya devuelve una clave de 256 bits que fuerza HS256
        return Jwts.builder()
                .header()
                .add("alg", ALLOWED_ALGORITHM) // Forzar algoritmo HS256 en el header
                .and()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey()) // La clave de 256 bits fuerza HS256 automáticamente
                .compact();
    }

    public String getTokenType(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.get("type", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Integer extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", Integer.class);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae los claims del token JWT con validación de seguridad
     * Valida explícitamente:
     * 1. Que el algoritmo no sea "none"
     * 2. Que el algoritmo sea el esperado (HS256)
     * 3. Que la firma sea válida
     * 
     * @param token Token JWT a validar
     * @return Claims del token
     * @throws io.jsonwebtoken.security.SecurityException si el algoritmo es inválido o la firma no coincide
     */
    /**
     * Extrae los claims del token JWT con validación de seguridad
     * Valida explícitamente:
     * 1. Que el algoritmo no sea "none"
     * 2. Que el algoritmo sea el esperado (HS256)
     * 3. Que la firma sea válida
     * 
     * Esto previene los siguientes ataques:
     * - Algoritmo "none" attack: Rechaza tokens sin firma
     * - Algorithm confusion attack: Solo acepta HS256, rechaza RSA/ECDSA
     * - Weak key attack: Valida longitud mínima de clave
     * 
     * @param token Token JWT a validar
     * @return Claims del token
     * @throws io.jsonwebtoken.security.SecurityException si el algoritmo es inválido o la firma no coincide
     */
    /**
     * Extrae el algoritmo del header del token JWT sin verificar la firma
     * Esto permite validar el algoritmo antes de verificar la firma
     */
    private String extractAlgorithmFromHeader(String token) {
        try {
            // Decodificar el header del JWT (primera parte del token)
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new io.jsonwebtoken.security.SecurityException("Token JWT inválido: formato incorrecto");
            }
            
            // Decodificar el header (base64url)
            String headerJson = new String(
                java.util.Base64.getUrlDecoder().decode(parts[0]),
                StandardCharsets.UTF_8
            );
            
            // Parsear el header JSON de forma simple (buscar "alg":)
            // Formato esperado: {"alg":"HS256","typ":"JWT"}
            int algIndex = headerJson.indexOf("\"alg\"");
            if (algIndex == -1) {
                return null; // No hay campo alg
            }
            
            // Buscar el valor después de "alg":
            int colonIndex = headerJson.indexOf(':', algIndex);
            if (colonIndex == -1) {
                return null;
            }
            
            // Buscar las comillas del valor
            int startQuote = headerJson.indexOf('"', colonIndex);
            if (startQuote == -1) {
                return null;
            }
            
            int endQuote = headerJson.indexOf('"', startQuote + 1);
            if (endQuote == -1) {
                return null;
            }
            
            return headerJson.substring(startQuote + 1, endQuote);
        } catch (Exception e) {
            throw new io.jsonwebtoken.security.SecurityException(
                "Error al decodificar el header del token JWT: " + e.getMessage(), e
            );
        }
    }

    private Claims extractAllClaims(String token) {
        try {
            // Validar el algoritmo ANTES de verificar la firma
            // Esto previene algorithm confusion attacks y algoritmo "none" attack
            String algorithm = extractAlgorithmFromHeader(token);
            
            // Validar que el algoritmo no sea "none"
            if (algorithm == null || "none".equalsIgnoreCase(algorithm)) {
                throw new io.jsonwebtoken.security.SecurityException(
                    "Algoritmo 'none' no permitido. Tokens sin firma son rechazados por seguridad."
                );
            }
            
            // Validar que el algoritmo sea el esperado (HS256)
            // Esto previene ataques de confusión de algoritmos (alg header attack)
            // donde un atacante intenta cambiar el algoritmo a RSA/ECDSA
            if (!ALLOWED_ALGORITHM.equals(algorithm)) {
                throw new io.jsonwebtoken.security.SecurityException(
                    String.format("Algoritmo '%s' no permitido. Solo se acepta '%s' (HMAC-SHA256). " +
                                "El sistema no confía en el header del token para determinar el algoritmo.", 
                        algorithm, ALLOWED_ALGORITHM)
                );
            }
            
            // Ahora verificar la firma con la clave correcta
            // Esto previene ataques de confusión de algoritmos (alg header attack)
            // JJWT 0.12.5 con verifyWith() ya valida que el algoritmo coincida con la clave,
            // pero nuestra validación explícita añade una capa adicional de seguridad
            return Jwts.parser()
                    .verifyWith(getSigningKey()) // Forzar uso de la clave HMAC
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (io.jsonwebtoken.security.SecurityException e) {
            // Re-lanzar excepciones de seguridad
            throw e;
        } catch (Exception e) {
            // Cualquier otro error se convierte en excepción de seguridad
            throw new io.jsonwebtoken.security.SecurityException(
                "Error al validar el token JWT: " + e.getMessage(), e
            );
        }
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, String email) {
        final String tokenEmail = extractEmail(token);
        return (tokenEmail.equals(email) && !isTokenExpired(token));
    }

    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public Integer extractTokenVersion(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.get("tokenVersion", Integer.class);
        } catch (Exception e) {
            return null;
        }
    }
}

