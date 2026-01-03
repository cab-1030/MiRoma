package com.miroma.miroma.service;

import com.miroma.miroma.entity.RefreshToken;
import com.miroma.miroma.entity.Usuario;
import com.miroma.miroma.repository.RefreshTokenRepository;
import com.miroma.miroma.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Optional;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtService jwtService;

    @Value("${jwt.refresh-expiration:604800000}") // 7 días por defecto
    private Long refreshExpiration;

    @Transactional
    public RefreshToken createRefreshToken(Integer usuarioId) {
        // Eliminar tokens anteriores del usuario
        refreshTokenRepository.deleteByUsuarioId(usuarioId);

        // Generar nuevo refresh token usando JWT
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String token = jwtService.generateRefreshToken(usuarioId, usuario.getEmail());
        
        // Calcular fecha de expiración
        Timestamp expiresAt = new Timestamp(System.currentTimeMillis() + refreshExpiration);

        // Crear y guardar el refresh token
        RefreshToken refreshToken = new RefreshToken(usuarioId, token, expiresAt);
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token no encontrado"));

        if (!refreshToken.isValid()) {
            throw new RuntimeException("Refresh token inválido o expirado");
        }

        // Validar el token JWT
        if (!jwtService.validateToken(token)) {
            throw new RuntimeException("Refresh token JWT inválido");
        }

        // Verificar que sea un refresh token
        String tokenType = jwtService.getTokenType(token);
        if (!"refresh".equals(tokenType)) {
            throw new RuntimeException("Token no es un refresh token");
        }

        return refreshToken;
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(token);
        if (refreshToken.isPresent()) {
            refreshToken.get().setRevoked(true);
            refreshTokenRepository.save(refreshToken.get());
        }
    }

    @Transactional
    public void revokeAllUserTokens(Integer usuarioId) {
        refreshTokenRepository.revokeByUsuarioId(usuarioId);
    }

    @Transactional
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(new Timestamp(System.currentTimeMillis()));
    }
}

