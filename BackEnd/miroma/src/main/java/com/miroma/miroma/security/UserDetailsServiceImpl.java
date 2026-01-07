package com.miroma.miroma.security;

import com.miroma.miroma.entity.Usuario;
import com.miroma.miroma.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * Implementación de UserDetailsService para Spring Security
 * Carga los detalles del usuario desde la base de datos
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        // Verificar que el usuario esté activo
        if (usuario.getActivo() == null || usuario.getActivo() != 1) {
            throw new UsernameNotFoundException("Usuario inactivo: " + email);
        }

        // Construir UserDetails con la información del usuario
        // Nota: La contraseña ya está hasheada, Spring Security la manejará
        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                .authorities(new ArrayList<>()) // Sin roles específicos por ahora
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(usuario.getActivo() != 1)
                .build();
    }

    /**
     * Carga un usuario por ID (útil para JWT)
     */
    public UserDetails loadUserById(Integer userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con ID: " + userId));

        if (usuario.getActivo() == null || usuario.getActivo() != 1) {
            throw new UsernameNotFoundException("Usuario inactivo con ID: " + userId);
        }

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                .authorities(new ArrayList<>())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(usuario.getActivo() != 1)
                .build();
    }
}


