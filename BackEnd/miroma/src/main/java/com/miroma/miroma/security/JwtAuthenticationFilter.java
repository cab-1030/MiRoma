package com.miroma.miroma.security;

import com.miroma.miroma.entity.Usuario;
import com.miroma.miroma.repository.UsuarioRepository;
import com.miroma.miroma.service.JwtService;
import com.miroma.miroma.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de Spring Security para autenticación JWT
 * Reemplaza el filtro anterior y se integra con Spring Security
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        // Intentar obtener el token de la cookie primero, luego del header
        String token = null;
        
        // Buscar token en cookies
        if (request.getCookies() != null) {
            String cookieNames = java.util.Arrays.stream(request.getCookies())
                .map(c -> c.getName() + "=" + (c.getValue() != null ? c.getValue().substring(0, Math.min(20, c.getValue().length())) + "..." : "null"))
                .collect(java.util.stream.Collectors.joining(", "));
            logger.info("Cookies recibidas en petición: " + cookieNames);
            
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    if (token != null) {
                        logger.info("Token encontrado en cookie (longitud: " + token.length() + ")");
                    } else {
                        logger.warn("Cookie accessToken encontrada pero sin valor");
                    }
                    break;
                }
            }
            if (token == null) {
                logger.warn("No se encontró cookie accessToken. Cookies disponibles: " + cookieNames);
            }
        } else {
            logger.warn("No hay cookies en la petición a: " + request.getRequestURI());
        }
        
        // Si no está en cookie, intentar del header Authorization (para compatibilidad)
        if (token == null || token.isEmpty()) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }
        
        // Si no hay token, continuar sin autenticación
        if (token == null || token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Verificar si el token está en la blacklist
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                // Token invalidado, no autenticar
                filterChain.doFilter(request, response);
                return;
            }

            // Validar el token
            if (jwtService.validateToken(token)) {
                String email = jwtService.extractEmail(token);
                Integer userId = jwtService.extractUserId(token);
                Integer tokenVersion = jwtService.extractTokenVersion(token);
                
                logger.debug("Token válido. Email: " + email + ", UserId: " + userId + ", TokenVersion: " + tokenVersion);
                
                // Verificar la versión del token contra la del usuario
                if (userId != null) {
                    Usuario usuario = usuarioRepository.findById(userId).orElse(null);
                    if (usuario != null) {
                        Integer userTokenVersion = usuario.getTokenVersion() != null ? usuario.getTokenVersion() : 1;
                        if (tokenVersion == null || !tokenVersion.equals(userTokenVersion)) {
                            // Token inválido por versión (contraseña fue cambiada)
                            logger.warn("Token version mismatch. Token version: " + tokenVersion + ", User version: " + userTokenVersion);
                            filterChain.doFilter(request, response);
                            return;
                        }
                    } else {
                        logger.warn("Usuario no encontrado para userId: " + userId);
                    }
                }
                
                // Cargar UserDetails solo si no hay autenticación en el contexto
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    try {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                        
                        // Crear autenticación y establecerla en el contexto de seguridad
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                            );
                        
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        logger.debug("Autenticación establecida para usuario: " + email);
                        
                        // Agregar información del usuario al request para compatibilidad con código existente
                        request.setAttribute("userId", userId);
                        request.setAttribute("email", email);
                    } catch (Exception e) {
                        logger.error("Error al cargar UserDetails para email: " + email, e);
                    }
                }
            } else {
                logger.warn("Token inválido o expirado");
            }
        } catch (Exception e) {
            // Si hay error, continuar sin autenticación (Spring Security manejará el acceso)
            logger.error("Error al validar token JWT", e);
        }
        
        filterChain.doFilter(request, response);
    }
}

