package com.miroma.miroma.controller;

import com.miroma.miroma.dto.PresupuestoRequest;
import com.miroma.miroma.dto.PresupuestoResponse;
import com.miroma.miroma.service.PresupuestoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/presupuestos")
public class PresupuestoController {

    @Autowired
    private PresupuestoService presupuestoService;

    @PostMapping
    public ResponseEntity<?> crearPresupuesto(
            @RequestAttribute("userId") Integer userId,
            @Valid @RequestBody PresupuestoRequest request) {
        try {
            PresupuestoResponse response = presupuestoService.crearPresupuesto(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al crear presupuesto: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping
    public ResponseEntity<?> obtenerPresupuestos(@RequestAttribute("userId") Integer userId) {
        try {
            List<PresupuestoResponse> presupuestos = presupuestoService.obtenerPresupuestosPorUsuario(userId);
            return ResponseEntity.ok(presupuestos);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener presupuestos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPresupuestoPorId(
            @PathVariable Integer id,
            @RequestAttribute("userId") Integer userId) {
        try {
            PresupuestoResponse presupuesto = presupuestoService.obtenerPresupuestoPorId(id, userId);
            return ResponseEntity.ok(presupuesto);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener presupuesto: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarPresupuesto(
            @PathVariable Integer id,
            @RequestAttribute("userId") Integer userId,
            @Valid @RequestBody PresupuestoRequest request) {
        try {
            PresupuestoResponse response = presupuestoService.actualizarPresupuesto(id, userId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al actualizar presupuesto: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPresupuesto(
            @PathVariable Integer id,
            @RequestAttribute("userId") Integer userId) {
        try {
            presupuestoService.eliminarPresupuesto(id, userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Presupuesto eliminado exitosamente");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al eliminar presupuesto: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

