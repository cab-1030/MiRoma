package com.miroma.miroma.controller;

import com.miroma.miroma.dto.IngresoRequest;
import com.miroma.miroma.dto.IngresoResponse;
import com.miroma.miroma.security.SecurityUtils;
import com.miroma.miroma.service.IngresoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ingresos")
public class IngresoController {

    @Autowired
    private IngresoService ingresoService;

    @Autowired
    private SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<?> crearIngreso(
            @Valid @RequestBody IngresoRequest request) {
        Integer userId = securityUtils.getCurrentUserId();
        IngresoResponse response = ingresoService.crearIngreso(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<?> obtenerIngresos() {
        Integer userId = securityUtils.getCurrentUserId();
        List<IngresoResponse> ingresos = ingresoService.obtenerIngresosPorUsuario(userId);
        return ResponseEntity.ok(ingresos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerIngresoPorId(@PathVariable Integer id) {
        Integer userId = securityUtils.getCurrentUserId();
        IngresoResponse ingreso = ingresoService.obtenerIngresoPorId(id, userId);
        return ResponseEntity.ok(ingreso);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarIngreso(
            @PathVariable Integer id,
            @Valid @RequestBody IngresoRequest request) {
        Integer userId = securityUtils.getCurrentUserId();
        IngresoResponse response = ingresoService.actualizarIngreso(id, userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarIngreso(@PathVariable Integer id) {
        Integer userId = securityUtils.getCurrentUserId();
        ingresoService.eliminarIngreso(id, userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Ingreso eliminado exitosamente");
        return ResponseEntity.ok(response);
    }
}

