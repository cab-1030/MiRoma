package com.miroma.miroma.controller;

import com.miroma.miroma.dto.PresupuestoCategoriaRequest;
import com.miroma.miroma.dto.PresupuestoCategoriaResponse;
import com.miroma.miroma.security.SecurityUtils;
import com.miroma.miroma.service.PresupuestoCategoriaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/presupuestos-categorias")
public class PresupuestoCategoriaController {

    @Autowired
    private PresupuestoCategoriaService presupuestoCategoriaService;

    @Autowired
    private SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<?> crearPresupuestoCategoria(
            @Valid @RequestBody PresupuestoCategoriaRequest request) {
        Integer userId = securityUtils.getCurrentUserId();
        try {
            PresupuestoCategoriaResponse response = presupuestoCategoriaService
                    .crearPresupuestoCategoria(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al crear presupuesto categoría: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/presupuesto/{presupuestoId}")
    public ResponseEntity<?> obtenerPresupuestosCategoriasPorPresupuesto(
            @PathVariable Integer presupuestoId) {
        Integer userId = securityUtils.getCurrentUserId();
        try {
            List<PresupuestoCategoriaResponse> presupuestosCategorias = presupuestoCategoriaService
                    .obtenerPresupuestosCategoriasPorPresupuesto(presupuestoId, userId);
            return ResponseEntity.ok(presupuestosCategorias);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener presupuestos categorías: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping
    public ResponseEntity<?> obtenerTodosLosPresupuestosCategorias() {
        Integer userId = securityUtils.getCurrentUserId();
        try {
            List<PresupuestoCategoriaResponse> presupuestosCategorias = presupuestoCategoriaService
                    .obtenerTodosLosPresupuestosCategorias(userId);
            return ResponseEntity.ok(presupuestosCategorias);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener presupuestos categorías: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPresupuestoCategoriaPorId(@PathVariable Integer id) {
        Integer userId = securityUtils.getCurrentUserId();
        try {
            PresupuestoCategoriaResponse presupuestoCategoria = presupuestoCategoriaService
                    .obtenerPresupuestoCategoriaPorId(id, userId);
            return ResponseEntity.ok(presupuestoCategoria);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener presupuesto categoría: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/presupuesto/{presupuestoId}/porcentaje-total")
    public ResponseEntity<?> obtenerPorcentajeTotalPorPresupuesto(
            @PathVariable Integer presupuestoId) {
        Integer userId = securityUtils.getCurrentUserId();
        try {
            BigDecimal porcentajeTotal = presupuestoCategoriaService
                    .obtenerPorcentajeTotalPorPresupuesto(presupuestoId, userId);
            Map<String, Object> response = new HashMap<>();
            response.put("porcentajeTotal", porcentajeTotal);
            response.put("porcentajeDisponible", new BigDecimal("100.00").subtract(porcentajeTotal));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener porcentaje total: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarPresupuestoCategoria(
            @PathVariable Integer id,
            @Valid @RequestBody PresupuestoCategoriaRequest request) {
        Integer userId = securityUtils.getCurrentUserId();
        try {
            PresupuestoCategoriaResponse response = presupuestoCategoriaService
                    .actualizarPresupuestoCategoria(id, userId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al actualizar presupuesto categoría: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPresupuestoCategoria(@PathVariable Integer id) {
        Integer userId = securityUtils.getCurrentUserId();
        try {
            presupuestoCategoriaService.eliminarPresupuestoCategoria(id, userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Presupuesto categoría eliminado exitosamente");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al eliminar presupuesto categoría: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/monto-disponible")
    public ResponseEntity<?> obtenerMontoDisponible(
            @RequestParam Integer presupuestoId,
            @RequestParam Integer categoriaId) {
        Integer userId = securityUtils.getCurrentUserId();
        try {
            // Obtener el usuario para validar permisos
            com.miroma.miroma.entity.Usuario usuario = presupuestoCategoriaService.obtenerUsuarioParaValidacion(userId);
            
            Integer parejaId = usuario.getParejaId();
            if (parejaId == null) {
                throw new IllegalArgumentException("Debes tener una pareja vinculada");
            }

            BigDecimal montoDisponible = presupuestoCategoriaService.calcularMontoDisponible(
                    parejaId, presupuestoId, categoriaId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("montoDisponible", montoDisponible);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener monto disponible: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

