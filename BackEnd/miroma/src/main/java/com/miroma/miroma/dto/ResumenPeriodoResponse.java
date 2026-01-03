package com.miroma.miroma.dto;

import java.math.BigDecimal;

public class ResumenPeriodoResponse {
    private Integer presupuestoId;
    private String periodo;
    private BigDecimal ingresosTotales;
    private BigDecimal egresosTotales;
    private BigDecimal dineroRestante;

    public ResumenPeriodoResponse() {
    }

    public ResumenPeriodoResponse(Integer presupuestoId, String periodo, BigDecimal ingresosTotales, 
                                  BigDecimal egresosTotales, BigDecimal dineroRestante) {
        this.presupuestoId = presupuestoId;
        this.periodo = periodo;
        this.ingresosTotales = ingresosTotales;
        this.egresosTotales = egresosTotales;
        this.dineroRestante = dineroRestante;
    }

    // Getters y Setters
    public Integer getPresupuestoId() {
        return presupuestoId;
    }

    public void setPresupuestoId(Integer presupuestoId) {
        this.presupuestoId = presupuestoId;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public BigDecimal getIngresosTotales() {
        return ingresosTotales;
    }

    public void setIngresosTotales(BigDecimal ingresosTotales) {
        this.ingresosTotales = ingresosTotales;
    }

    public BigDecimal getEgresosTotales() {
        return egresosTotales;
    }

    public void setEgresosTotales(BigDecimal egresosTotales) {
        this.egresosTotales = egresosTotales;
    }

    public BigDecimal getDineroRestante() {
        return dineroRestante;
    }

    public void setDineroRestante(BigDecimal dineroRestante) {
        this.dineroRestante = dineroRestante;
    }
}

