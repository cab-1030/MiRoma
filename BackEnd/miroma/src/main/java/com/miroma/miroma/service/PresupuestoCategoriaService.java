package com.miroma.miroma.service;

import com.miroma.miroma.dto.PresupuestoCategoriaRequest;
import com.miroma.miroma.dto.PresupuestoCategoriaResponse;
import com.miroma.miroma.entity.CategoriaEgreso;
import com.miroma.miroma.entity.Presupuesto;
import com.miroma.miroma.entity.PresupuestoCategoria;
import com.miroma.miroma.entity.Usuario;
import com.miroma.miroma.entity.Egreso;
import com.miroma.miroma.entity.Ingreso;
import com.miroma.miroma.repository.CategoriaEgresoRepository;
import com.miroma.miroma.repository.EgresoRepository;
import com.miroma.miroma.repository.IngresoRepository;
import com.miroma.miroma.repository.PresupuestoCategoriaRepository;
import com.miroma.miroma.repository.PresupuestoRepository;
import com.miroma.miroma.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PresupuestoCategoriaService {

    @Autowired
    private PresupuestoCategoriaRepository presupuestoCategoriaRepository;

    @Autowired
    private PresupuestoRepository presupuestoRepository;

    @Autowired
    private CategoriaEgresoRepository categoriaEgresoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private IngresoRepository ingresoRepository;

    @Autowired
    private EgresoRepository egresoRepository;

    @Autowired
    private LogEventoService logEventoService;

    @Transactional
    public PresupuestoCategoriaResponse crearPresupuestoCategoria(Integer userId, PresupuestoCategoriaRequest request) {
        // Validar que el usuario existe
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Validar que el presupuesto existe y pertenece a la pareja del usuario
        Presupuesto presupuesto = presupuestoRepository.findById(request.getPresupuestoId())
                .orElseThrow(() -> new IllegalArgumentException("Presupuesto no encontrado"));

        if (usuario.getParejaId() == null || !presupuesto.getParejaId().equals(usuario.getParejaId())) {
            throw new IllegalArgumentException("No tienes permiso para acceder a este presupuesto");
        }

        // Validar que la categoría existe
        CategoriaEgreso categoria = categoriaEgresoRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        // Validar que no existe ya esta categoría en este presupuesto
        if (presupuestoCategoriaRepository.existsByPresupuestoIdAndCategoriaId(
                request.getPresupuestoId(), request.getCategoriaId())) {
            throw new IllegalArgumentException("Esta categoría ya está asignada a este presupuesto");
        }

        // Validar que el porcentaje no exceda el 100% total
        BigDecimal porcentajeActual = presupuestoCategoriaRepository.sumarPorcentajesPorPresupuesto(
                request.getPresupuestoId());
        BigDecimal nuevoTotal = porcentajeActual.add(request.getPorcentaje());

        if (nuevoTotal.compareTo(new BigDecimal("100.00")) > 0) {
            BigDecimal disponible = new BigDecimal("100.00").subtract(porcentajeActual);
            throw new IllegalArgumentException(
                    String.format("El porcentaje excede el 100%%. Porcentaje disponible: %.2f%%", disponible));
        }

        // Crear el presupuesto categoría
        PresupuestoCategoria presupuestoCategoria = new PresupuestoCategoria();
        presupuestoCategoria.setPresupuestoId(request.getPresupuestoId());
        presupuestoCategoria.setCategoriaId(request.getCategoriaId());
        presupuestoCategoria.setPorcentaje(request.getPorcentaje().setScale(2, RoundingMode.HALF_UP));

        presupuestoCategoria = presupuestoCategoriaRepository.save(presupuestoCategoria);
        presupuestoCategoriaRepository.flush();

        // Calcular ingresos totales de la pareja
        BigDecimal ingresosTotales = calcularIngresosTotalesPareja(presupuesto.getParejaId());

        // Registrar evento en log
        logEventoService.registrarEvento(userId, 
            String.format("Asignó %s%% del presupuesto '%s' a la categoría '%s'", 
                presupuestoCategoria.getPorcentaje(), presupuesto.getPeriodo(), categoria.getNombre()));

        return mapToResponse(presupuestoCategoria, presupuesto, categoria, ingresosTotales);
    }

    public List<PresupuestoCategoriaResponse> obtenerPresupuestosCategoriasPorPresupuesto(
            Integer presupuestoId, Integer userId) {
        // Validar que el usuario existe
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Validar que el presupuesto existe y pertenece a la pareja del usuario
        Presupuesto presupuesto = presupuestoRepository.findById(presupuestoId)
                .orElseThrow(() -> new IllegalArgumentException("Presupuesto no encontrado"));

        if (usuario.getParejaId() == null || !presupuesto.getParejaId().equals(usuario.getParejaId())) {
            throw new IllegalArgumentException("No tienes permiso para acceder a este presupuesto");
        }

        List<PresupuestoCategoria> presupuestosCategorias = presupuestoCategoriaRepository
                .findByPresupuestoId(presupuestoId);

        // Calcular ingresos totales de la pareja
        BigDecimal ingresosTotales = calcularIngresosTotalesPareja(presupuesto.getParejaId());

        return presupuestosCategorias.stream()
                .map(pc -> {
                    CategoriaEgreso categoria = categoriaEgresoRepository.findById(pc.getCategoriaId())
                            .orElse(null);
                    return mapToResponse(pc, presupuesto, categoria, ingresosTotales);
                })
                .collect(Collectors.toList());
    }

    public List<PresupuestoCategoriaResponse> obtenerTodosLosPresupuestosCategorias(Integer userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (usuario.getParejaId() == null) {
            throw new IllegalArgumentException("Debes tener una pareja vinculada para ver presupuestos por categoría");
        }

        // Obtener todos los presupuestos de la pareja
        List<Presupuesto> presupuestos = presupuestoRepository
                .findByParejaIdOrderByFechaCreacionDesc(usuario.getParejaId());

        // Calcular ingresos totales de la pareja
        BigDecimal ingresosTotales = calcularIngresosTotalesPareja(usuario.getParejaId());

        // Obtener todos los presupuestos categorías de esos presupuestos
        return presupuestos.stream()
                .flatMap(presupuesto -> {
                    List<PresupuestoCategoria> pcs = presupuestoCategoriaRepository
                            .findByPresupuestoId(presupuesto.getId());
                    return pcs.stream().map(pc -> {
                        CategoriaEgreso categoria = categoriaEgresoRepository.findById(pc.getCategoriaId())
                                .orElse(null);
                        return mapToResponse(pc, presupuesto, categoria, ingresosTotales);
                    });
                })
                .collect(Collectors.toList());
    }

    public PresupuestoCategoriaResponse obtenerPresupuestoCategoriaPorId(Integer id, Integer userId) {
        PresupuestoCategoria presupuestoCategoria = presupuestoCategoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Presupuesto categoría no encontrado"));

        // Validar que el usuario tiene acceso
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Presupuesto presupuesto = presupuestoRepository.findById(presupuestoCategoria.getPresupuestoId())
                .orElseThrow(() -> new IllegalArgumentException("Presupuesto no encontrado"));

        if (usuario.getParejaId() == null || !presupuesto.getParejaId().equals(usuario.getParejaId())) {
            throw new IllegalArgumentException("No tienes permiso para acceder a este presupuesto categoría");
        }

        CategoriaEgreso categoria = categoriaEgresoRepository.findById(presupuestoCategoria.getCategoriaId())
                .orElse(null);

        // Calcular ingresos totales de la pareja
        BigDecimal ingresosTotales = calcularIngresosTotalesPareja(presupuesto.getParejaId());

        return mapToResponse(presupuestoCategoria, presupuesto, categoria, ingresosTotales);
    }

    @Transactional
    public PresupuestoCategoriaResponse actualizarPresupuestoCategoria(
            Integer id, Integer userId, PresupuestoCategoriaRequest request) {
        PresupuestoCategoria presupuestoCategoria = presupuestoCategoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Presupuesto categoría no encontrado"));

        // Validar que el usuario tiene acceso
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Presupuesto presupuesto = presupuestoRepository.findById(presupuestoCategoria.getPresupuestoId())
                .orElseThrow(() -> new IllegalArgumentException("Presupuesto no encontrado"));

        if (usuario.getParejaId() == null || !presupuesto.getParejaId().equals(usuario.getParejaId())) {
            throw new IllegalArgumentException("No tienes permiso para modificar este presupuesto categoría");
        }

        // Validar que la categoría existe
        CategoriaEgreso categoria = categoriaEgresoRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        // Si se cambia la categoría, validar que no exista ya en el presupuesto
        if (!presupuestoCategoria.getCategoriaId().equals(request.getCategoriaId())) {
            if (presupuestoCategoriaRepository.existsByPresupuestoIdAndCategoriaId(
                    presupuestoCategoria.getPresupuestoId(), request.getCategoriaId())) {
                throw new IllegalArgumentException("Esta categoría ya está asignada a este presupuesto");
            }
        }

        // Validar que el porcentaje no exceda el 100% total (excluyendo el registro actual)
        BigDecimal porcentajeActual = presupuestoCategoriaRepository
                .sumarPorcentajesPorPresupuestoExcluyendo(
                        presupuestoCategoria.getPresupuestoId(), id);
        BigDecimal nuevoTotal = porcentajeActual.add(request.getPorcentaje());

        if (nuevoTotal.compareTo(new BigDecimal("100.00")) > 0) {
            BigDecimal disponible = new BigDecimal("100.00").subtract(porcentajeActual);
            throw new IllegalArgumentException(
                    String.format("El porcentaje excede el 100%%. Porcentaje disponible: %.2f%%", disponible));
        }

        // Actualizar
        presupuestoCategoria.setCategoriaId(request.getCategoriaId());
        presupuestoCategoria.setPorcentaje(request.getPorcentaje().setScale(2, RoundingMode.HALF_UP));

        presupuestoCategoria = presupuestoCategoriaRepository.save(presupuestoCategoria);
        presupuestoCategoriaRepository.flush();

        // Calcular ingresos totales de la pareja
        BigDecimal ingresosTotales = calcularIngresosTotalesPareja(presupuesto.getParejaId());

        // Registrar evento en log
        logEventoService.registrarEvento(userId, 
            String.format("Actualizó la asignación de presupuesto (ID: %d) - Categoría: '%s', Porcentaje: %s%%", 
                id, categoria.getNombre(), presupuestoCategoria.getPorcentaje()));

        return mapToResponse(presupuestoCategoria, presupuesto, categoria, ingresosTotales);
    }

    @Transactional
    public void eliminarPresupuestoCategoria(Integer id, Integer userId) {
        PresupuestoCategoria presupuestoCategoria = presupuestoCategoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Presupuesto categoría no encontrado"));

        // Validar que el usuario tiene acceso
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Presupuesto presupuesto = presupuestoRepository.findById(presupuestoCategoria.getPresupuestoId())
                .orElseThrow(() -> new IllegalArgumentException("Presupuesto no encontrado"));

        if (usuario.getParejaId() == null || !presupuesto.getParejaId().equals(usuario.getParejaId())) {
            throw new IllegalArgumentException("No tienes permiso para eliminar este presupuesto categoría");
        }

        // Obtener información de la categoría antes de eliminar
        CategoriaEgreso categoria = categoriaEgresoRepository.findById(presupuestoCategoria.getCategoriaId())
                .orElse(null);
        String categoriaNombre = categoria != null ? categoria.getNombre() : "Desconocida";

        // Registrar evento en log antes de eliminar
        logEventoService.registrarEvento(userId, 
            String.format("Eliminó la asignación de presupuesto (ID: %d) - Categoría: '%s', Período: '%s'", 
                id, categoriaNombre, presupuesto.getPeriodo()));

        presupuestoCategoriaRepository.delete(presupuestoCategoria);
    }

    public BigDecimal obtenerPorcentajeTotalPorPresupuesto(Integer presupuestoId, Integer userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Presupuesto presupuesto = presupuestoRepository.findById(presupuestoId)
                .orElseThrow(() -> new IllegalArgumentException("Presupuesto no encontrado"));

        if (usuario.getParejaId() == null || !presupuesto.getParejaId().equals(usuario.getParejaId())) {
            throw new IllegalArgumentException("No tienes permiso para acceder a este presupuesto");
        }

        return presupuestoCategoriaRepository.sumarPorcentajesPorPresupuesto(presupuestoId);
    }

    private BigDecimal calcularIngresosTotalesPareja(Integer parejaId) {
        List<Ingreso> ingresos = ingresoRepository.findByParejaIdOrderByFechaDesc(parejaId);
        return ingresos.stream()
                .map(Ingreso::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcularEgresosPorCategoria(Integer parejaId, Integer categoriaId, Integer presupuestoId) {
        List<Egreso> egresos = egresoRepository.findByParejaIdOrderByFechaDesc(parejaId);
        return egresos.stream()
                .filter(egreso -> egreso.getCategoriaId().equals(categoriaId))
                .filter(egreso -> egreso.getPeriodoId().equals(presupuestoId))
                .map(Egreso::getMontoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private PresupuestoCategoriaResponse mapToResponse(
            PresupuestoCategoria presupuestoCategoria, Presupuesto presupuesto, 
            CategoriaEgreso categoria, BigDecimal ingresosTotales) {
        PresupuestoCategoriaResponse response = new PresupuestoCategoriaResponse();
        response.setId(presupuestoCategoria.getId());
        response.setPresupuestoId(presupuestoCategoria.getPresupuestoId());
        response.setPresupuestoPeriodo(presupuesto != null ? presupuesto.getPeriodo() : null);
        response.setCategoriaId(presupuestoCategoria.getCategoriaId());
        response.setCategoriaNombre(categoria != null ? categoria.getNombre() : null);
        response.setCategoriaDescripcion(categoria != null ? categoria.getDescripcion() : null);
        response.setPorcentaje(presupuestoCategoria.getPorcentaje());

        // Calcular montos
        if (presupuesto != null && ingresosTotales != null) {
            // Monto asignado = (porcentaje / 100) * ingresos totales
            BigDecimal montoAsignado = ingresosTotales
                    .multiply(presupuestoCategoria.getPorcentaje())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            response.setMontoAsignado(montoAsignado);

            // Monto gastado = suma de egresos de esta categoría que pertenecen a este presupuesto
            BigDecimal montoGastado = calcularEgresosPorCategoria(
                    presupuesto.getParejaId(), 
                    presupuestoCategoria.getCategoriaId(), 
                    presupuestoCategoria.getPresupuestoId());
            response.setMontoGastado(montoGastado);

            // Monto disponible = asignado - gastado
            BigDecimal montoDisponible = montoAsignado.subtract(montoGastado);
            response.setMontoDisponible(montoDisponible);
        } else {
            response.setMontoAsignado(BigDecimal.ZERO);
            response.setMontoGastado(BigDecimal.ZERO);
            response.setMontoDisponible(BigDecimal.ZERO);
        }

        return response;
    }
}

