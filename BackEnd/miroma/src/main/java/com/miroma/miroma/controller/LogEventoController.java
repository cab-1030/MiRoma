package com.miroma.miroma.controller;

import com.miroma.miroma.dto.LogEventoResponse;
import com.miroma.miroma.entity.LogEvento;
import com.miroma.miroma.repository.LogEventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/log-eventos")
public class LogEventoController {

    @Autowired
    private LogEventoRepository logEventoRepository;

    @GetMapping
    public ResponseEntity<?> obtenerEventosPorUsuario(
            @RequestAttribute("userId") Integer userId,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false) String tipoAccion) {
        try {
            List<LogEvento> eventos;
            
            // Si hay filtros de fecha, convertir a Timestamp
            if (fechaInicio != null && fechaFin != null && !fechaInicio.isEmpty() && !fechaFin.isEmpty()) {
                Timestamp fechaInicioTs = Timestamp.valueOf(fechaInicio + " 00:00:00");
                Timestamp fechaFinTs = Timestamp.valueOf(fechaFin + " 23:59:59");
                
                if (tipoAccion != null && !tipoAccion.isEmpty()) {
                    eventos = logEventoRepository.findByUsuarioIdAndFechaBetweenAndAccionContaining(
                            userId, fechaInicioTs, fechaFinTs, tipoAccion);
                } else {
                    eventos = logEventoRepository.findByUsuarioIdAndFechaBetween(
                            userId, fechaInicioTs, fechaFinTs);
                }
            } else if (tipoAccion != null && !tipoAccion.isEmpty()) {
                eventos = logEventoRepository.findByUsuarioIdAndAccionContaining(userId, tipoAccion);
            } else {
                eventos = logEventoRepository.findByUsuarioIdOrderByFechaDesc(userId);
            }
            
            List<LogEventoResponse> response = eventos.stream()
                    .map(evento -> new LogEventoResponse(
                            evento.getId(),
                            evento.getUsuarioId(),
                            evento.getAccion(),
                            evento.getFecha()
                    ))
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener eventos: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}

