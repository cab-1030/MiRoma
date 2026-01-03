package com.miroma.miroma.dto;

import java.math.BigDecimal;

public class PresupuestoCategoriaResponse {
    private Integer id;
    private Integer presupuestoId;
    private String presupuestoPeriodo;
    private Integer categoriaId;
    private String categoriaNombre;
    private String categoriaDescripcion;
    private BigDecimal porcentaje;
    private BigDecimal montoAsignado;      // Monto total asignado según el porcentaje
    private BigDecimal montoGastado;       // Monto gastado en esta categoría
    private BigDecimal montoDisponible;    // Monto disponible (asignado - gastado)

    public PresupuestoCategoriaResponse() {
    }

    public PresupuestoCategoriaResponse(Integer id, Integer presupuestoId, String presupuestoPeriodo,
                                       Integer categoriaId, String categoriaNombre, String categoriaDescripcion,
                                       BigDecimal porcentaje) {
        this.id = id;
        this.presupuestoId = presupuestoId;
        this.presupuestoPeriodo = presupuestoPeriodo;
        this.categoriaId = categoriaId;
        this.categoriaNombre = categoriaNombre;
        this.categoriaDescripcion = categoriaDescripcion;
        this.porcentaje = porcentaje;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPresupuestoId() {
        return presupuestoId;
    }

    public void setPresupuestoId(Integer presupuestoId) {
        this.presupuestoId = presupuestoId;
    }

    public String getPresupuestoPeriodo() {
        return presupuestoPeriodo;
    }

    public void setPresupuestoPeriodo(String presupuestoPeriodo) {
        this.presupuestoPeriodo = presupuestoPeriodo;
    }

    public Integer getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Integer categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }

    public String getCategoriaDescripcion() {
        return categoriaDescripcion;
    }

    public void setCategoriaDescripcion(String categoriaDescripcion) {
        this.categoriaDescripcion = categoriaDescripcion;
    }

    public BigDecimal getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(BigDecimal porcentaje) {
        this.porcentaje = porcentaje;
    }

    public BigDecimal getMontoAsignado() {
        return montoAsignado;
    }

    public void setMontoAsignado(BigDecimal montoAsignado) {
        this.montoAsignado = montoAsignado;
    }

    public BigDecimal getMontoGastado() {
        return montoGastado;
    }

    public void setMontoGastado(BigDecimal montoGastado) {
        this.montoGastado = montoGastado;
    }

    public BigDecimal getMontoDisponible() {
        return montoDisponible;
    }

    public void setMontoDisponible(BigDecimal montoDisponible) {
        this.montoDisponible = montoDisponible;
    }
}

