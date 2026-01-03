package com.miroma.miroma.service;

import com.miroma.miroma.dto.ResumenPeriodoResponse;
import com.miroma.miroma.entity.*;
import com.miroma.miroma.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResumenFinancieroService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PresupuestoRepository presupuestoRepository;

    @Autowired
    private IngresoRepository ingresoRepository;

    @Autowired
    private EgresoParticipacionRepository egresoParticipacionRepository;

    @Autowired
    private EgresoRepository egresoRepository;

    public List<ResumenPeriodoResponse> obtenerResumenPorUsuario(Integer userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Integer parejaId = usuario.getParejaId();
        if (parejaId == null) {
            throw new IllegalArgumentException("Debes tener una pareja vinculada para ver el resumen financiero");
        }

        // Calcular ingresos totales del usuario (sin filtrar por período)
        BigDecimal ingresosTotalesUsuario = calcularIngresosTotalesUsuario(userId);

        // Obtener todos los presupuestos de la pareja
        List<Presupuesto> presupuestos = presupuestoRepository.findByParejaIdOrderByFechaCreacionDesc(parejaId);

        return presupuestos.stream()
                .map(presupuesto -> {
                    // Calcular egresos totales del usuario para este período (suma de participaciones)
                    BigDecimal egresosTotales = calcularEgresosTotalesUsuario(userId, presupuesto.getId());

                    // Calcular dinero restante
                    BigDecimal dineroRestante = ingresosTotalesUsuario.subtract(egresosTotales);

                    return new ResumenPeriodoResponse(
                            presupuesto.getId(),
                            presupuesto.getPeriodo(),
                            ingresosTotalesUsuario,
                            egresosTotales,
                            dineroRestante
                    );
                })
                .collect(Collectors.toList());
    }

    private BigDecimal calcularIngresosTotalesUsuario(Integer usuarioId) {
        List<Ingreso> ingresos = ingresoRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);
        return ingresos.stream()
                .map(Ingreso::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcularEgresosTotalesUsuario(Integer usuarioId, Integer presupuestoId) {
        // Obtener todas las participaciones del usuario
        List<EgresoParticipacion> participaciones = egresoParticipacionRepository.findByUsuarioId(usuarioId);
        
        return participaciones.stream()
                .filter(participacion -> {
                    // Obtener el egreso para verificar su periodoId
                    Egreso egreso = egresoRepository.findById(participacion.getEgresoId())
                            .orElse(null);
                    return egreso != null && egreso.getPeriodoId().equals(presupuestoId);
                })
                .map(EgresoParticipacion::getMontoAsignado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

