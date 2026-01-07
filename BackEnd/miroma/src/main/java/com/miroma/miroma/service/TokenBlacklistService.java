package com.miroma.miroma.service;

import com.miroma.miroma.entity.TokenBlacklist;
import com.miroma.miroma.repository.TokenBlacklistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Servicio para manejar la blacklist de tokens JWT invalidados
 */
@Service
public class TokenBlacklistService {

    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Autowired
    private JwtService jwtService;

    /**
     * Genera un hash SHA-256 del token para almacenarlo de forma segura
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al generar hash del token", e);
        }
    }

    /**
     * Invalida un token agregándolo a la blacklist
     * @param token Token JWT a invalidar
     * @param usuarioId ID del usuario propietario del token
     */
    @Transactional
    public void invalidateToken(String token, Integer usuarioId) {
        try {
            // Obtener la fecha de expiración del token
            Date expirationDate = jwtService.extractExpiration(token);
            if (expirationDate == null) {
                return; // Token inválido, no hacer nada
            }

            // Generar hash del token
            String tokenHash = hashToken(token);

            // Verificar si ya está en la blacklist
            if (tokenBlacklistRepository.existsByTokenHash(tokenHash)) {
                return; // Ya está invalidado
            }

            // Crear entrada en blacklist
            TokenBlacklist blacklistEntry = new TokenBlacklist(
                tokenHash,
                usuarioId,
                new Timestamp(expirationDate.getTime())
            );

            tokenBlacklistRepository.save(blacklistEntry);
        } catch (Exception e) {
            // Log error pero no lanzar excepción para no interrumpir el flujo
            System.err.println("Error al invalidar token: " + e.getMessage());
        }
    }

    /**
     * Verifica si un token está en la blacklist
     * @param token Token JWT a verificar
     * @return true si el token está invalidado, false si es válido
     */
    public boolean isTokenBlacklisted(String token) {
        try {
            String tokenHash = hashToken(token);
            return tokenBlacklistRepository.existsByTokenHash(tokenHash);
        } catch (Exception e) {
            // En caso de error, asumir que no está en blacklist para no bloquear acceso legítimo
            return false;
        }
    }

    /**
     * Invalida todos los tokens de un usuario (útil cuando cambia la contraseña)
     * @param usuarioId ID del usuario
     */
    @Transactional
    public void invalidateAllUserTokens(Integer usuarioId) {
        try {
            // Eliminar todos los tokens del usuario de la blacklist
            // (aunque técnicamente no están en blacklist, esto limpia entradas antiguas)
            tokenBlacklistRepository.deleteAllByUsuarioId(usuarioId);
        } catch (Exception e) {
            System.err.println("Error al invalidar tokens del usuario: " + e.getMessage());
        }
    }

    /**
     * Limpia tokens expirados de la blacklist (ejecutado periódicamente)
     */
    @Scheduled(fixedRate = 3600000) // Cada hora
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            tokenBlacklistRepository.deleteExpiredTokens(now);
        } catch (Exception e) {
            System.err.println("Error al limpiar tokens expirados: " + e.getMessage());
        }
    }
}


