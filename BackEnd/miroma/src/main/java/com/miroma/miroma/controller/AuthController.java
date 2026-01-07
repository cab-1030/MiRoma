package com.miroma.miroma.controller;

import com.miroma.miroma.dto.ChangePasswordRequest;
import com.miroma.miroma.dto.CheckUserRequest;
import com.miroma.miroma.dto.CheckUserResponse;
import com.miroma.miroma.dto.LinkPartnerRequest;
import com.miroma.miroma.dto.LinkPartnerResponse;
import com.miroma.miroma.dto.LoginRequest;
import com.miroma.miroma.dto.LoginResponse;
import com.miroma.miroma.dto.RefreshTokenRequest;
import com.miroma.miroma.dto.RefreshTokenResponse;
import com.miroma.miroma.dto.RegisterRequest;
import com.miroma.miroma.dto.RegisterResponse;
import com.miroma.miroma.entity.Usuario;
import com.miroma.miroma.repository.UsuarioRepository;
import com.miroma.miroma.security.SecurityUtils;
import com.miroma.miroma.service.JwtService;
import com.miroma.miroma.service.RefreshTokenService;
import com.miroma.miroma.service.TokenBlacklistService;
import com.miroma.miroma.service.UsuarioService;
import com.miroma.miroma.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private CookieUtil cookieUtil;

    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "AuthController está funcionando correctamente");
        response.put("endpoint", "/api/auth/register");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletResponse httpResponse) {
        LoginResponse response = usuarioService.login(request);
        
        // Establecer cookies HTTP-only, Secure y SameSite
        cookieUtil.setAccessTokenCookie(httpResponse, response.getToken());
        if (response.getRefreshToken() != null) {
            cookieUtil.setRefreshTokenCookie(httpResponse, response.getRefreshToken());
        }
        
        // No enviar tokens en el body por seguridad (aunque los mantenemos para compatibilidad temporal)
        // En producción, considera remover los tokens del response body
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = usuarioService.registrarUsuario(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @RequestBody(required = false) RefreshTokenRequest request,
            @CookieValue(value = "refreshToken", required = false) String refreshTokenFromCookie,
            HttpServletResponse httpResponse) {
        
        // Usar refresh token de cookie si está disponible, sino del body
        String refreshToken = null;
        
        if (refreshTokenFromCookie != null && !refreshTokenFromCookie.isEmpty()) {
            refreshToken = refreshTokenFromCookie;
        } else if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isEmpty()) {
            refreshToken = request.getRefreshToken();
        }
        
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new com.miroma.miroma.exception.UnauthorizedException("Refresh token no proporcionado");
        }
        // Verificar el refresh token
        com.miroma.miroma.entity.RefreshToken refreshTokenEntity = refreshTokenService.verifyRefreshToken(refreshToken);
        
        // Obtener el usuario
        Usuario usuario = usuarioRepository.findById(refreshTokenEntity.getUsuarioId())
                .orElseThrow(() -> new com.miroma.miroma.exception.ResourceNotFoundException("usuario", refreshTokenEntity.getUsuarioId()));

        // Verificar que el usuario esté activo
        if (usuario.getActivo() == null || usuario.getActivo() != 1) {
            throw new com.miroma.miroma.exception.UnauthorizedException("Usuario inactivo");
        }

        // Generar nuevo access token con versión de token
        Integer tokenVersion = usuario.getTokenVersion() != null ? usuario.getTokenVersion() : 1;
        String newAccessToken = jwtService.generateToken(usuario.getId(), usuario.getEmail(), usuario.getNombre(), tokenVersion);

        // Generar nuevo refresh token (rotación de tokens)
        com.miroma.miroma.entity.RefreshToken newRefreshTokenEntity = refreshTokenService.createRefreshToken(usuario.getId());
        String newRefreshToken = newRefreshTokenEntity.getToken();

        // Revocar el refresh token anterior
        refreshTokenService.revokeRefreshToken(refreshToken);

        // Establecer nuevas cookies
        cookieUtil.setAccessTokenCookie(httpResponse, newAccessToken);
        cookieUtil.setRefreshTokenCookie(httpResponse, newRefreshToken);

        // Crear respuesta
        RefreshTokenResponse response = new RefreshTokenResponse();
        response.setToken(newAccessToken);
        response.setRefreshToken(newRefreshToken);
        response.setMensaje("Tokens renovados exitosamente");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/check-user")
    public ResponseEntity<?> checkUser(@Valid @RequestBody CheckUserRequest request) {
        CheckUserResponse response = usuarioService.checkUserExists(request.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/link-partner")
    public ResponseEntity<?> linkPartner(
            @Valid @RequestBody LinkPartnerRequest request) {
        Integer userId = securityUtils.getCurrentUserId();
        LinkPartnerResponse response = usuarioService.linkPartner(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @CookieValue(value = "accessToken", required = false) String accessTokenFromCookie,
            @CookieValue(value = "refreshToken", required = false) String refreshTokenFromCookie,
            HttpServletResponse httpResponse) {
        try {
            // Obtener token de cookie o header
            String token = null;
            if (accessTokenFromCookie != null && !accessTokenFromCookie.isEmpty()) {
                token = accessTokenFromCookie;
            } else if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }

            // Invalidar el access token si está presente
            if (token != null) {
                try {
                    Integer userId = jwtService.extractUserId(token);
                    if (userId != null) {
                        tokenBlacklistService.invalidateToken(token, userId);
                    }
                } catch (Exception e) {
                    // Si no se puede extraer el userId, continuar con el logout
                }
            }

            // Revocar el refresh token si está presente
            if (refreshTokenFromCookie != null && !refreshTokenFromCookie.isEmpty()) {
                try {
                    refreshTokenService.revokeRefreshToken(refreshTokenFromCookie);
                } catch (Exception e) {
                    // Continuar con el logout aunque falle la revocación
                }
            }
            
            // Eliminar cookies
            cookieUtil.deleteTokenCookies(httpResponse);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Logout exitoso");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Aún así eliminar cookies y devolver éxito
            cookieUtil.deleteTokenCookies(httpResponse);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Logout exitoso");
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @CookieValue(value = "accessToken", required = false) String accessTokenFromCookie,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletResponse httpResponse) {
        Integer userId = securityUtils.getCurrentUserId();
        usuarioService.changePassword(userId, request);
        
        // Invalidar el token actual agregándolo a la blacklist
        String token = null;
        if (accessTokenFromCookie != null && !accessTokenFromCookie.isEmpty()) {
            token = accessTokenFromCookie;
        } else if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        
        if (token != null) {
            try {
                tokenBlacklistService.invalidateToken(token, userId);
            } catch (Exception e) {
                // Continuar aunque falle la invalidación
            }
        }
        
        // Eliminar cookies para forzar nuevo login
        cookieUtil.deleteTokenCookies(httpResponse);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Contraseña cambiada exitosamente. Por favor, inicia sesión nuevamente.");
        return ResponseEntity.ok(response);
    }

}

