package com.miroma.miroma.service;

import com.miroma.miroma.entity.LogEvento;
import com.miroma.miroma.repository.LogEventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogEventoService {

    @Autowired
    private LogEventoRepository logEventoRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarEvento(Integer usuarioId, String accion) {
        try {
            LogEvento logEvento = new LogEvento(usuarioId, accion);
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

