package com.miroma.miroma.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PresupuestoRequest {
    
    @NotBlank(message = "El periodo es obligatorio")
    @Size(max = 50, message = "El periodo no puede exceder 50 caracteres")
    private String periodo;

    public PresupuestoRequest() {
    }

    public PresupuestoRequest(String periodo) {
        this.periodo = periodo;
    }

    // Getters y Setters
    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }
}

