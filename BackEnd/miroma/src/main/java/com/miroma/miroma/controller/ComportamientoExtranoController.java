package com.miroma.miroma.controller;

import com.miroma.miroma.dto.ComportamientoExtranoResponse;
import com.miroma.miroma.service.ComportamientoExtranoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/comportamientos-extranos")
public class ComportamientoExtranoController {

    @Autowired
    private ComportamientoExtranoService comportamientoExtranoService;

    @GetMapping
    public ResponseEntity<List<ComportamientoExtranoResponse>> obtenerComportamientosExtranos() {
        List<ComportamientoExtranoResponse> comportamientos = comportamientoExtranoService.obtenerComportamientosExtranos();
        return ResponseEntity.ok(comportamientos);
    }
}


