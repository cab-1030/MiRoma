package com.miroma.miroma.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class EgresoRequest {
    
    @NotNull(message = "El monto total es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto total debe ser mayor a 0")
    private BigDecimal montoTotal;

    @NotNull(message = "La fecha es obligatoria")
    private String fecha;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    @NotNull(message = "La categoría es obligatoria")
    @Positive(message = "El ID de la categoría debe ser positivo")
    private Integer categoriaId;

    @NotNull(message = "El período es obligatorio")
    @Positive(message = "El ID del período debe ser positivo")
    private Integer periodoId;

    public EgresoRequest() {
    }

    public EgresoRequest(BigDecimal montoTotal, String fecha, String descripcion, Integer categoriaId) {
        this.montoTotal = montoTotal;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.categoriaId = categoriaId;
    }

    // Getters y Setters
    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Integer categoriaId) {
        this.categoriaId = categoriaId;
    }

    public Integer getPeriodoId() {
        return periodoId;
    }

    public void setPeriodoId(Integer periodoId) {
        this.periodoId = periodoId;
    }
}

