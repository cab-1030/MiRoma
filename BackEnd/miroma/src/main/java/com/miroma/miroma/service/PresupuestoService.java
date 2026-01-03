package com.miroma.miroma.service;

import com.miroma.miroma.dto.PresupuestoRequest;
import com.miroma.miroma.dto.PresupuestoResponse;
import com.miroma.miroma.entity.Presupuesto;
import com.miroma.miroma.entity.Usuario;
import com.miroma.miroma.repository.PresupuestoRepository;
import com.miroma.miroma.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PresupuestoService {

    @Autowired
    private PresupuestoRepository presupuestoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LogEventoService logEventoService;

    @Transactional
    public PresupuestoResponse crearPresupuesto(Integer userId, PresupuestoRequest request) {
        // Obtener el usuario
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Obtener pareja_id del usuario
        Integer parejaId = usuario.getParejaId();
        
        if (parejaId == null) {
            throw new IllegalArgumentException("Debes tener una pareja vinculada para crear presupuestos");
        }

        // Verificar si ya existe un presupuesto para este periodo y pareja
        if (presupuestoRepository.existsByParejaIdAndPeriodo(parejaId, request.getPeriodo().trim())) {
            throw new IllegalArgumentException("Ya existe un presupuesto para el periodo: " + request.getPeriodo());
        }

        // Crear el presupuesto
        Presupuesto presupuesto = new Presupuesto();
        presupuesto.setParejaId(parejaId);
        presupuesto.setPeriodo(request.getPeriodo().trim());

        presupuesto = presupuestoRepository.save(presupuesto);
        presupuestoRepository.flush();

        return mapToResponse(presupuesto);
    }

    public List<PresupuestoResponse> obtenerPresupuestosPorUsuario(Integer userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Integer parejaId = usuario.getParejaId();
        
        if (parejaId == null) {
            throw new IllegalArgumentException("Debes tener una pareja vinculada para ver presupuestos");
        }

        List<Presupuesto> presupuestos = presupuestoRepository.findByParejaIdOrderByFechaCreacionDesc(parejaId);

        return presupuestos.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PresupuestoResponse obtenerPresupuestoPorId(Integer presupuestoId, Integer userId) {
        Presupuesto presupuesto = presupuestoRepository.findById(presupuestoId)
                .orElseThrow(() -> new IllegalArgumentException("Presupuesto no encontrado"));

        // Verificar que el presupuesto pertenezca a la pareja del usuario
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (usuario.getParejaId() == null || !presupuesto.getParejaId().equals(usuario.getParejaId())) {
            throw new IllegalArgumentException("No tienes permiso para acceder a este presupuesto");
        }

        return mapToResponse(presupuesto);
    }

    @Transactional
    public PresupuestoResponse actualizarPresupuesto(Integer presupuestoId, Integer userId, PresupuestoRequest request) {
        Presupuesto presupuesto = presupuestoRepository.findById(presupuestoId)
                .orElseThrow(() -> new IllegalArgumentException("Presupuesto no encontrado"));

        // Verificar que el presupuesto pertenezca a la pareja del usuario
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (usuario.getParejaId() == null || !presupuesto.getParejaId().equals(usuario.getParejaId())) {
            throw new IllegalArgumentException("No tienes permiso para modificar este presupuesto");
        }

        // Verificar si el nuevo periodo ya existe para esta pareja (excepto el actual)
        String nuevoPeriodo = request.getPeriodo().trim();
        if (!presupuesto.getPeriodo().equals(nuevoPeriodo)) {
            if (presupuestoRepository.existsByParejaIdAndPeriodo(presupuesto.getParejaId(), nuevoPeriodo)) {
                throw new IllegalArgumentException("Ya existe un presupuesto para el periodo: " + nuevoPeriodo);
            }
        }

        // Actualizar periodo
        presupuesto.setPeriodo(nuevoPeriodo);

        presupuesto = presupuestoRepository.save(presupuesto);
        presupuestoRepository.flush();

        // Registrar evento en log
        logEventoService.registrarEvento(userId, 
            String.format("Actualizó el presupuesto (ID: %d) - Nuevo período: '%s'", presupuestoId, presupuesto.getPeriodo()));

        return mapToResponse(presupuesto);
    }

    @Transactional
    public void eliminarPresupuesto(Integer presupuestoId, Integer userId) {
        Presupuesto presupuesto = presupuestoRepository.findById(presupuestoId)
                .orElseThrow(() -> new IllegalArgumentException("Presupuesto no encontrado"));

        // Verificar que el presupuesto pertenezca a la pareja del usuario
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (usuario.getParejaId() == null || !presupuesto.getParejaId().equals(usuario.getParejaId())) {
            throw new IllegalArgumentException("No tienes permiso para eliminar este presupuesto");
        }

        // Registrar evento en log antes de eliminar
        logEventoService.registrarEvento(userId, 
            String.format("Eliminó el presupuesto (ID: %d) - Período: '%s'", presupuestoId, presupuesto.getPeriodo()));

        presupuestoRepository.delete(presupuesto);
    }

    private PresupuestoResponse mapToResponse(Presupuesto presupuesto) {
        PresupuestoResponse response = new PresupuestoResponse();
        response.setId(presupuesto.getId());
        response.setParejaId(presupuesto.getParejaId());
        response.setPeriodo(presupuesto.getPeriodo());
        response.setFechaCreacion(presupuesto.getFechaCreacion());
        return response;
    }
}

