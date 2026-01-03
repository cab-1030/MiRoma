package com.miroma.miroma.dto;

import java.sql.Timestamp;

public class PresupuestoResponse {
    private Integer id;
    private Integer parejaId;
    private String periodo;
    private Timestamp fechaCreacion;

    public PresupuestoResponse() {
    }

    public PresupuestoResponse(Integer id, Integer parejaId, String periodo, Timestamp fechaCreacion) {
        this.id = id;
        this.parejaId = parejaId;
        this.periodo = periodo;
        this.fechaCreacion = fechaCreacion;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getParejaId() {
        return parejaId;
    }

    public void setParejaId(Integer parejaId) {
        this.parejaId = parejaId;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}

