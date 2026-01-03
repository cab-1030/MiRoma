package com.miroma.miroma.service;

import com.miroma.miroma.dto.IngresoRequest;
import com.miroma.miroma.dto.IngresoResponse;
import com.miroma.miroma.entity.Ingreso;
import com.miroma.miroma.entity.Usuario;
import com.miroma.miroma.exception.UnauthorizedException;
import com.miroma.miroma.repository.IngresoRepository;
import com.miroma.miroma.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IngresoService {

    @Autowired
    private IngresoRepository ingresoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LogEventoService logEventoService;

    @Transactional
    public IngresoResponse crearIngreso(Integer userId, IngresoRequest request) {
        // Obtener el usuario
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Obtener pareja_id del usuario
        Integer parejaId = usuario.getParejaId();

        // Crear el ingreso
        Ingreso ingreso = new Ingreso();
        ingreso.setUsuarioId(userId);
        ingreso.setParejaId(parejaId);
        ingreso.setMonto(request.getMonto());
        ingreso.setDescripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null);
        ingreso.setFecha(Date.valueOf(request.getFecha()));

        ingreso = ingresoRepository.save(ingreso);
        ingresoRepository.flush();

        // Registrar evento en log
        logEventoService.registrarEvento(userId, 
            String.format("Creó un ingreso de $%s - %s", request.getMonto(), 
                request.getDescripcion() != null ? request.getDescripcion() : "Sin descripción"));

        return mapToResponse(ingreso);
    }

    public List<IngresoResponse> obtenerIngresosPorUsuario(Integer userId) {
        // Solo obtener ingresos del usuario logueado, no de su pareja
        List<Ingreso> ingresos = ingresoRepository.findByUsuarioIdOrderByFechaDesc(userId);

        return ingresos.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public IngresoResponse obtenerIngresoPorId(Integer ingresoId, Integer userId) {
        Ingreso ingreso = ingresoRepository.findById(ingresoId)
                .orElseThrow(() -> new IllegalArgumentException("Ingreso no encontrado"));

        // Verificar que el ingreso pertenezca solo al usuario logueado
        if (!ingreso.getUsuarioId().equals(userId)) {
            throw new UnauthorizedException("No tienes permiso para acceder a este ingreso");
        }

        return mapToResponse(ingreso);
    }

    @Transactional
    public IngresoResponse actualizarIngreso(Integer ingresoId, Integer userId, IngresoRequest request) {
        Ingreso ingreso = ingresoRepository.findById(ingresoId)
                .orElseThrow(() -> new IllegalArgumentException("Ingreso no encontrado"));

        // Verificar que el ingreso pertenezca al usuario
        if (!ingreso.getUsuarioId().equals(userId)) {
            throw new IllegalArgumentException("No tienes permiso para modificar este ingreso");
        }

        // Actualizar campos
        ingreso.setMonto(request.getMonto());
        ingreso.setDescripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null);
        ingreso.setFecha(Date.valueOf(request.getFecha()));

        ingreso = ingresoRepository.save(ingreso);
        ingresoRepository.flush();

        // Registrar evento en log
        logEventoService.registrarEvento(userId, 
            String.format("Actualizó un ingreso (ID: %d) - Monto: $%s", ingresoId, request.getMonto()));

        return mapToResponse(ingreso);
    }

    @Transactional
    public void eliminarIngreso(Integer ingresoId, Integer userId) {
        Ingreso ingreso = ingresoRepository.findById(ingresoId)
                .orElseThrow(() -> new IllegalArgumentException("Ingreso no encontrado"));

        // Verificar que el ingreso pertenezca al usuario
        if (!ingreso.getUsuarioId().equals(userId)) {
            throw new IllegalArgumentException("No tienes permiso para eliminar este ingreso");
        }

        // Registrar evento en log antes de eliminar
        logEventoService.registrarEvento(userId, 
            String.format("Eliminó un ingreso (ID: %d) - Monto: $%s", ingresoId, ingreso.getMonto()));

        ingresoRepository.delete(ingreso);
    }

    private IngresoResponse mapToResponse(Ingreso ingreso) {
        IngresoResponse response = new IngresoResponse();
        response.setId(ingreso.getId());
        response.setUsuarioId(ingreso.getUsuarioId());
        response.setParejaId(ingreso.getParejaId());
        response.setMonto(ingreso.getMonto());
        response.setDescripcion(ingreso.getDescripcion());
        response.setFecha(ingreso.getFecha());
        response.setFechaCreacion(ingreso.getFechaCreacion());
        return response;
    }
}

