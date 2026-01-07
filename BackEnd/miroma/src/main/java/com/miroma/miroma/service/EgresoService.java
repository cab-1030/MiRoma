package com.miroma.miroma.service;

import com.miroma.miroma.dto.EgresoRequest;
import com.miroma.miroma.dto.EgresoResponse;
import com.miroma.miroma.entity.*;
import com.miroma.miroma.repository.*;
import com.miroma.miroma.entity.PresupuestoCategoria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EgresoService {

    @Autowired
    private EgresoRepository egresoRepository;

    @Autowired
    private EgresoParticipacionRepository egresoParticipacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaEgresoRepository categoriaEgresoRepository;

    @Autowired
    private IngresoRepository ingresoRepository;

    @Autowired
    private ParejaRepository parejaRepository;

    @Autowired
    private PresupuestoRepository presupuestoRepository;

    @Autowired
    private PresupuestoCategoriaRepository presupuestoCategoriaRepository;

    @Autowired
    private PresupuestoCategoriaService presupuestoCategoriaService;

    @Autowired
    private LogEventoService logEventoService;

    @Transactional
    public EgresoResponse crearEgreso(Integer userId, EgresoRequest request) {
        // Obtener el usuario
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Obtener pareja_id del usuario
        Integer parejaId = usuario.getParejaId();
        if (parejaId == null) {
            throw new IllegalArgumentException("Debes tener una pareja vinculada para crear egresos");
        }

        // Validar que la categoría existe
        CategoriaEgreso categoria = categoriaEgresoRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        // Validar que el período existe y pertenece a la pareja
        Presupuesto presupuesto = presupuestoRepository.findById(request.getPeriodoId())
                .orElseThrow(() -> new IllegalArgumentException("Período no encontrado"));
        
        if (!presupuesto.getParejaId().equals(parejaId)) {
            throw new IllegalArgumentException("El período no pertenece a tu pareja");
        }

        // Validar que existe un presupuesto para esta categoría en este período
        PresupuestoCategoria presupuestoCategoria = presupuestoCategoriaRepository
                .findByPresupuestoIdAndCategoriaId(request.getPeriodoId(), request.getCategoriaId())
                .orElse(null);
        
        if (presupuestoCategoria == null) {
            throw new IllegalArgumentException("No existe un presupuesto asignado para esta categoría en el período seleccionado");
        }

        // Calcular el monto disponible antes de crear el egreso
        BigDecimal montoDisponible = presupuestoCategoriaService.calcularMontoDisponible(
                parejaId, request.getPeriodoId(), request.getCategoriaId());
        
        // Validar que el monto del egreso no exceda el disponible
        if (request.getMontoTotal().compareTo(montoDisponible) > 0) {
            throw new IllegalArgumentException("No es posible crear este egreso porque supera los límites definidos");
        }

        // Crear el egreso
        Egreso egreso = new Egreso();
        egreso.setParejaId(parejaId);
        egreso.setMontoTotal(request.getMontoTotal());
        egreso.setFecha(Date.valueOf(request.getFecha()));
        egreso.setDescripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null);
        egreso.setCategoriaId(request.getCategoriaId());
        egreso.setPeriodoId(request.getPeriodoId());

        egreso = egresoRepository.save(egreso);
        egresoRepository.flush();

        // Crear las participaciones automáticamente
        crearParticipaciones(egreso, parejaId);

        // Registrar evento en log
        logEventoService.registrarEvento(userId, 
            String.format("Creó un egreso de $%s en categoría '%s' para el período '%s'", 
                request.getMontoTotal(), categoria.getNombre(), presupuesto.getPeriodo()));

        return mapToResponse(egreso, categoria, presupuesto);
    }

    private void crearParticipaciones(Egreso egreso, Integer parejaId) {
        // Obtener la pareja por su ID (parejaId es el id de la tabla parejas)
        Pareja pareja = parejaRepository.findById(parejaId)
                .orElseThrow(() -> new IllegalArgumentException("Pareja no encontrada"));

        // Obtener los usuarios de la pareja
        Usuario esposo = usuarioRepository.findById(pareja.getEsposoId())
                .orElseThrow(() -> new IllegalArgumentException("Esposo no encontrado"));
        Usuario esposa = usuarioRepository.findById(pareja.getEsposaId())
                .orElseThrow(() -> new IllegalArgumentException("Esposa no encontrada"));

        // Calcular ingresos totales de cada uno
        BigDecimal ingresosEsposo = calcularIngresosTotales(esposo.getId(), parejaId);
        BigDecimal ingresosEsposa = calcularIngresosTotales(esposa.getId(), parejaId);
        BigDecimal ingresosTotales = ingresosEsposo.add(ingresosEsposa);

        // Si no hay ingresos, dividir 50/50
        BigDecimal porcentajeEsposo;
        BigDecimal porcentajeEsposa;
        if (ingresosTotales.compareTo(BigDecimal.ZERO) == 0) {
            porcentajeEsposo = new BigDecimal("50.00");
            porcentajeEsposa = new BigDecimal("50.00");
        } else {
            // Calcular porcentajes basados en ingresos
            porcentajeEsposo = ingresosEsposo
                    .divide(ingresosTotales, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
            porcentajeEsposa = new BigDecimal("100.00").subtract(porcentajeEsposo);
        }

        // Calcular montos asignados
        BigDecimal montoAsignadoEsposo = egreso.getMontoTotal()
                .multiply(porcentajeEsposo)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal montoAsignadoEsposa = egreso.getMontoTotal().subtract(montoAsignadoEsposo);

        // Crear participación del esposo
        EgresoParticipacion participacionEsposo = new EgresoParticipacion();
        participacionEsposo.setEgresoId(egreso.getId());
        participacionEsposo.setUsuarioId(esposo.getId());
        participacionEsposo.setPorcentaje(porcentajeEsposo);
        participacionEsposo.setMontoAsignado(montoAsignadoEsposo);
        egresoParticipacionRepository.save(participacionEsposo);

        // Crear participación de la esposa
        EgresoParticipacion participacionEsposa = new EgresoParticipacion();
        participacionEsposa.setEgresoId(egreso.getId());
        participacionEsposa.setUsuarioId(esposa.getId());
        participacionEsposa.setPorcentaje(porcentajeEsposa);
        participacionEsposa.setMontoAsignado(montoAsignadoEsposa);
        egresoParticipacionRepository.save(participacionEsposa);

        egresoParticipacionRepository.flush();
    }

    private BigDecimal calcularIngresosTotales(Integer usuarioId, Integer parejaId) {
        // Obtener todos los ingresos de la pareja
        List<Ingreso> ingresos = ingresoRepository.findByParejaIdOrderByFechaDesc(parejaId);

        // Filtrar ingresos del usuario específico y sumar
        return ingresos.stream()
                .filter(ingreso -> ingreso.getUsuarioId().equals(usuarioId))
                .map(Ingreso::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcularIngresosTotalesPareja(Integer parejaId) {
        List<Ingreso> ingresos = ingresoRepository.findByParejaIdOrderByFechaDesc(parejaId);
        return ingresos.stream()
                .map(Ingreso::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula los egresos por categoría excluyendo un egreso específico
     * Útil para validar al actualizar un egreso
     */
    private BigDecimal calcularEgresosPorCategoriaExcluyendo(
            Integer parejaId, Integer categoriaId, Integer presupuestoId, Integer egresoIdExcluir) {
        List<Egreso> egresos = egresoRepository.findByParejaIdOrderByFechaDesc(parejaId);
        return egresos.stream()
                .filter(egreso -> egreso.getCategoriaId().equals(categoriaId))
                .filter(egreso -> egreso.getPeriodoId().equals(presupuestoId))
                .filter(egreso -> !egreso.getId().equals(egresoIdExcluir)) // Excluir el egreso actual
                .map(Egreso::getMontoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<EgresoResponse> obtenerEgresosPorUsuario(Integer userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Integer parejaId = usuario.getParejaId();
        if (parejaId == null) {
            throw new IllegalArgumentException("Debes tener una pareja vinculada para ver egresos");
        }

        List<Egreso> egresos = egresoRepository.findByParejaIdOrderByFechaDesc(parejaId);

        return egresos.stream()
                .map(egreso -> {
                    CategoriaEgreso categoria = categoriaEgresoRepository.findById(egreso.getCategoriaId())
                            .orElse(null);
                    Presupuesto presupuesto = presupuestoRepository.findById(egreso.getPeriodoId())
                            .orElse(null);
                    return mapToResponse(egreso, categoria, presupuesto);
                })
                .collect(Collectors.toList());
    }

    public List<EgresoResponse> obtenerEgresosPorUsuarioYFecha(Integer userId, String fechaInicio, String fechaFin) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Integer parejaId = usuario.getParejaId();
        if (parejaId == null) {
            throw new IllegalArgumentException("Debes tener una pareja vinculada para ver egresos");
        }

        Date fechaInicioDate = Date.valueOf(fechaInicio);
        Date fechaFinDate = Date.valueOf(fechaFin);

        List<Egreso> egresos = egresoRepository.findByParejaIdAndFechaBetween(parejaId, fechaInicioDate, fechaFinDate);

        return egresos.stream()
                .map(egreso -> {
                    CategoriaEgreso categoria = categoriaEgresoRepository.findById(egreso.getCategoriaId())
                            .orElse(null);
                    Presupuesto presupuesto = presupuestoRepository.findById(egreso.getPeriodoId())
                            .orElse(null);
                    return mapToResponse(egreso, categoria, presupuesto);
                })
                .collect(Collectors.toList());
    }

    public EgresoResponse obtenerEgresoPorId(Integer egresoId, Integer userId) {
        Egreso egreso = egresoRepository.findById(egresoId)
                .orElseThrow(() -> new IllegalArgumentException("Egreso no encontrado"));

        // Verificar que el egreso pertenezca a la pareja del usuario
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (usuario.getParejaId() == null || !egreso.getParejaId().equals(usuario.getParejaId())) {
            throw new IllegalArgumentException("No tienes permiso para acceder a este egreso");
        }

        CategoriaEgreso categoria = categoriaEgresoRepository.findById(egreso.getCategoriaId())
                .orElse(null);
        Presupuesto presupuesto = presupuestoRepository.findById(egreso.getPeriodoId())
                .orElse(null);

        return mapToResponse(egreso, categoria, presupuesto);
    }

    @Transactional
    public EgresoResponse actualizarEgreso(Integer egresoId, Integer userId, EgresoRequest request) {
        Egreso egreso = egresoRepository.findById(egresoId)
                .orElseThrow(() -> new IllegalArgumentException("Egreso no encontrado"));

        // Verificar que el egreso pertenezca a la pareja del usuario
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (usuario.getParejaId() == null || !egreso.getParejaId().equals(usuario.getParejaId())) {
            throw new IllegalArgumentException("No tienes permiso para modificar este egreso");
        }

        // Validar que la categoría existe
        CategoriaEgreso categoria = categoriaEgresoRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        // Validar que el período existe y pertenece a la pareja
        Presupuesto presupuesto = presupuestoRepository.findById(request.getPeriodoId())
                .orElseThrow(() -> new IllegalArgumentException("Período no encontrado"));
        
        if (!presupuesto.getParejaId().equals(egreso.getParejaId())) {
            throw new IllegalArgumentException("El período no pertenece a tu pareja");
        }

        // Validar que existe un presupuesto para esta categoría en este período
        PresupuestoCategoria presupuestoCategoria = presupuestoCategoriaRepository
                .findByPresupuestoIdAndCategoriaId(request.getPeriodoId(), request.getCategoriaId())
                .orElse(null);
        
        if (presupuestoCategoria == null) {
            throw new IllegalArgumentException("No existe un presupuesto asignado para esta categoría en el período seleccionado");
        }

        // Calcular el monto disponible antes de actualizar el egreso
        // Necesitamos excluir el egreso actual del cálculo de monto gastado
        BigDecimal montoGastadoActual = calcularEgresosPorCategoriaExcluyendo(
                egreso.getParejaId(), request.getCategoriaId(), request.getPeriodoId(), egresoId);
        
        // Calcular ingresos totales
        BigDecimal ingresosTotales = calcularIngresosTotalesPareja(egreso.getParejaId());
        
        // Calcular monto asignado
        BigDecimal montoAsignado = ingresosTotales
                .multiply(presupuestoCategoria.getPorcentaje())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        
        // Monto disponible = asignado - gastado (sin incluir el egreso actual)
        BigDecimal montoDisponible = montoAsignado.subtract(montoGastadoActual);
        
        // Validar que el nuevo monto del egreso no exceda el disponible
        if (request.getMontoTotal().compareTo(montoDisponible) > 0) {
            throw new IllegalArgumentException("No es posible crear este egreso porque supera los límites definidos");
        }

        // Actualizar el egreso
        egreso.setMontoTotal(request.getMontoTotal());
        egreso.setFecha(Date.valueOf(request.getFecha()));
        egreso.setDescripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null);
        egreso.setCategoriaId(request.getCategoriaId());
        egreso.setPeriodoId(request.getPeriodoId());

        egreso = egresoRepository.save(egreso);
        egresoRepository.flush();

        // Eliminar participaciones existentes y crear nuevas
        egresoParticipacionRepository.deleteByEgresoId(egresoId);
        crearParticipaciones(egreso, egreso.getParejaId());

        // Registrar evento en log
        logEventoService.registrarEvento(userId, 
            String.format("Actualizó un egreso (ID: %d) - Monto: $%s, Categoría: '%s'", 
                egresoId, request.getMontoTotal(), categoria.getNombre()));

        return mapToResponse(egreso, categoria, presupuesto);
    }

    @Transactional
    public void eliminarEgreso(Integer egresoId, Integer userId) {
        Egreso egreso = egresoRepository.findById(egresoId)
                .orElseThrow(() -> new IllegalArgumentException("Egreso no encontrado"));

        // Verificar que el egreso pertenezca a la pareja del usuario
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (usuario.getParejaId() == null || !egreso.getParejaId().equals(usuario.getParejaId())) {
            throw new IllegalArgumentException("No tienes permiso para eliminar este egreso");
        }

        // Registrar evento en log antes de eliminar
        CategoriaEgreso categoria = categoriaEgresoRepository.findById(egreso.getCategoriaId()).orElse(null);
        String categoriaNombre = categoria != null ? categoria.getNombre() : "Desconocida";
        logEventoService.registrarEvento(userId, 
            String.format("Eliminó un egreso (ID: %d) - Monto: $%s, Categoría: '%s'", 
                egresoId, egreso.getMontoTotal(), categoriaNombre));

        // Eliminar participaciones primero (por las foreign keys)
        egresoParticipacionRepository.deleteByEgresoId(egresoId);
        
        // Eliminar el egreso
        egresoRepository.delete(egreso);
    }

    private EgresoResponse mapToResponse(Egreso egreso, CategoriaEgreso categoria, Presupuesto presupuesto) {
        EgresoResponse response = new EgresoResponse();
        response.setId(egreso.getId());
        response.setParejaId(egreso.getParejaId());
        response.setMontoTotal(egreso.getMontoTotal());
        response.setFecha(egreso.getFecha().toString());
        response.setDescripcion(egreso.getDescripcion());
        response.setCategoriaId(egreso.getCategoriaId());
        response.setCategoriaNombre(categoria != null ? categoria.getNombre() : null);
        response.setCategoriaDescripcion(categoria != null ? categoria.getDescripcion() : null);
        response.setPeriodoId(egreso.getPeriodoId());
        response.setPeriodoNombre(presupuesto != null ? presupuesto.getPeriodo() : null);
        response.setFechaCreacion(egreso.getFechaCreacion());
        return response;
    }
}

