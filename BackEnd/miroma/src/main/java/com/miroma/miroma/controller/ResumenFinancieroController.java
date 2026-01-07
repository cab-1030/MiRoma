package com.miroma.miroma.controller;

import com.miroma.miroma.dto.ResumenPeriodoResponse;
import com.miroma.miroma.security.SecurityUtils;
import com.miroma.miroma.service.ResumenFinancieroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resumen-financiero")
public class ResumenFinancieroController {

    @Autowired
    private ResumenFinancieroService resumenFinancieroService;

    @Autowired
    private SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<?> obtenerResumen() {
        Integer userId = securityUtils.getCurrentUserId();
        try {
            List<ResumenPeriodoResponse> resumen = resumenFinancieroService.obtenerResumenPorUsuario(userId);
            return ResponseEntity.ok(resumen);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener resumen financiero: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}

