package com.miroma.miroma.controller;

import com.miroma.miroma.dto.EgresoRequest;
import com.miroma.miroma.dto.EgresoResponse;
import com.miroma.miroma.security.SecurityUtils;
import com.miroma.miroma.service.EgresoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/egresos")
public class EgresoController {

    @Autowired
    private EgresoService egresoService;

    @Autowired
    private SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<?> crearEgreso(
            @Valid @RequestBody EgresoRequest request) {
        Integer userId = securityUtils.getCurrentUserId();
        try {
            EgresoResponse response = egresoService.crearEgreso(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al crear egreso: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping
    public ResponseEntity<?> obtenerEgresos(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin) {
        Integer userId = securityUtils.getCurrentUserId();
        try {
            List<EgresoResponse> egresos;
            if (fechaInicio != null && fechaFin != null) {
                egresos = egresoService.obtenerEgresosPorUsuarioYFecha(userId, fechaInicio, fechaFin);
            } else {
                egresos = egresoService.obtenerEgresosPorUsuario(userId);
            }
            return ResponseEntity.ok(egresos);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener egresos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerEgresoPorId(@PathVariable Integer id) {
        Integer userId = securityUtils.getCurrentUserId();
        try {
            EgresoResponse egreso = egresoService.obtenerEgresoPorId(id, userId);
            return ResponseEntity.ok(egreso);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener egreso: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarEgreso(
            @PathVariable Integer id,
            @Valid @RequestBody EgresoRequest request) {
        Integer userId = securityUtils.getCurrentUserId();
        try {
            EgresoResponse response = egresoService.actualizarEgreso(id, userId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al actualizar egreso: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarEgreso(@PathVariable Integer id) {
        Integer userId = securityUtils.getCurrentUserId();
        try {
            egresoService.eliminarEgreso(id, userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Egreso eliminado exitosamente");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al eliminar egreso: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

