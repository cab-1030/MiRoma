package com.miroma.miroma.controller;

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
import com.miroma.miroma.service.JwtService;
import com.miroma.miroma.service.RefreshTokenService;
import com.miroma.miroma.service.UsuarioService;
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

    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "AuthController está funcionando correctamente");
        response.put("endpoint", "/api/auth/register");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = usuarioService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = usuarioService.registrarUsuario(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        // Verificar el refresh token
        com.miroma.miroma.entity.RefreshToken refreshTokenEntity = refreshTokenService.verifyRefreshToken(request.getRefreshToken());
        
        // Obtener el usuario
        Usuario usuario = usuarioRepository.findById(refreshTokenEntity.getUsuarioId())
                .orElseThrow(() -> new com.miroma.miroma.exception.ResourceNotFoundException("usuario", refreshTokenEntity.getUsuarioId()));

        // Verificar que el usuario esté activo
        if (usuario.getActivo() == null || usuario.getActivo() != 1) {
            throw new com.miroma.miroma.exception.UnauthorizedException("Usuario inactivo");
        }

        // Generar nuevo access token
        String newAccessToken = jwtService.generateToken(usuario.getId(), usuario.getEmail(), usuario.getNombre());

        // Generar nuevo refresh token (rotación de tokens)
        com.miroma.miroma.entity.RefreshToken newRefreshTokenEntity = refreshTokenService.createRefreshToken(usuario.getId());
        String newRefreshToken = newRefreshTokenEntity.getToken();

        // Revocar el refresh token anterior
        refreshTokenService.revokeRefreshToken(request.getRefreshToken());

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
            @RequestAttribute("userId") Integer userId,
            @Valid @RequestBody LinkPartnerRequest request) {
        LinkPartnerResponse response = usuarioService.linkPartner(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody(required = false) Map<String, String> request) {
        try {
            // Si se proporciona un refresh token, revocarlo
            if (request != null && request.containsKey("refreshToken")) {
                refreshTokenService.revokeRefreshToken(request.get("refreshToken"));
            }
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Logout exitoso");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Aún así devolver éxito, el logout local es lo más importante
            Map<String, String> response = new HashMap<>();
            response.put("message", "Logout exitoso");
            return ResponseEntity.ok(response);
        }
    }

}

