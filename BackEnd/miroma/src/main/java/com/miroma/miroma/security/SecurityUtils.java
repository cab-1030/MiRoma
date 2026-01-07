package com.miroma.miroma.security;

import com.miroma.miroma.entity.Usuario;
import com.miroma.miroma.repository.UsuarioRepository;
import com.miroma.miroma.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Utilidades de seguridad para obtener información del usuario autenticado
 */
@Component
public class SecurityUtils {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Obtiene el ID del usuario autenticado
     * @return ID del usuario
     * @throws ResourceNotFoundException Si el usuario no se encuentra
     */
    public Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new com.miroma.miroma.exception.UnauthorizedException("Usuario no autenticado");
        }
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Usuario usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("usuario", userDetails.getUsername()));
        
        return usuario.getId();
    }

    /**
     * Obtiene el email del usuario autenticado
     * @return Email del usuario
     */
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new com.miroma.miroma.exception.UnauthorizedException("Usuario no autenticado");
        }
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }

    /**
     * Obtiene el objeto Usuario completo del usuario autenticado
     * @return Usuario
     * @throws ResourceNotFoundException Si el usuario no se encuentra
     */
    public Usuario getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new com.miroma.miroma.exception.UnauthorizedException("Usuario no autenticado");
        }
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("usuario", userDetails.getUsername()));
    }
}


