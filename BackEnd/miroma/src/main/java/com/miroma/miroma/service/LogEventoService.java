package com.miroma.miroma.service;

import com.miroma.miroma.entity.LogEvento;
import com.miroma.miroma.entity.Usuario;
import com.miroma.miroma.repository.LogEventoRepository;
import com.miroma.miroma.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class LogEventoService {

    @Autowired
    private LogEventoRepository logEventoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private static final int MAX_ACCION_LENGTH = 1000;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarEvento(Integer usuarioId, String accion) {
        try {
            // Obtener el nombre del usuario
            String nombreUsuario = "Usuario desconocido";
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
            if (usuarioOpt.isPresent()) {
                nombreUsuario = usuarioOpt.get().getNombre();
            }

            // Construir el mensaje completo con el nombre del usuario
            String accionCompleta = String.format("%s: %s", nombreUsuario, accion);

            // Truncar la acción si excede el límite
            String accionTruncada = accionCompleta;
            if (accionCompleta != null && accionCompleta.length() > MAX_ACCION_LENGTH) {
                accionTruncada = accionCompleta.substring(0, MAX_ACCION_LENGTH - 3) + "...";
            }
            
            LogEvento logEvento = new LogEvento(usuarioId, accionTruncada);
            logEventoRepository.save(logEvento);
            logEventoRepository.flush();
        } catch (Exception e) {
            // No lanzar excepción para no interrumpir el flujo principal
            // Solo loggear el error
            System.err.println("Error al registrar evento en log: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

