package com.miroma.miroma.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class PresupuestoCategoriaRequest {
    
    @NotNull(message = "El ID del presupuesto es obligatorio")
    @Positive(message = "El ID del presupuesto debe ser positivo")
    private Integer presupuestoId;

    @NotNull(message = "El ID de la categoría es obligatorio")
    @Positive(message = "El ID de la categoría debe ser positivo")
    private Integer categoriaId;

    @NotNull(message = "El porcentaje es obligatorio")
    @DecimalMin(value = "0.01", message = "El porcentaje debe ser mayor a 0")
    @DecimalMax(value = "100.00", message = "El porcentaje no puede exceder 100%")
    private BigDecimal porcentaje;

    public PresupuestoCategoriaRequest() {
    }

    public PresupuestoCategoriaRequest(Integer presupuestoId, Integer categoriaId, BigDecimal porcentaje) {
        this.presupuestoId = presupuestoId;
        this.categoriaId = categoriaId;
        this.porcentaje = porcentaje;
    }

    // Getters y Setters
    public Integer getPresupuestoId() {
        return presupuestoId;
    }

    public void setPresupuestoId(Integer presupuestoId) {
        this.presupuestoId = presupuestoId;
    }

    public Integer getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Integer categoriaId) {
        this.categoriaId = categoriaId;
    }

    public BigDecimal getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(BigDecimal porcentaje) {
        this.porcentaje = porcentaje;
    }
}

