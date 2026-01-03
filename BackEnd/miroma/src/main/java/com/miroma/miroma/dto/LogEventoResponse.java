package com.miroma.miroma.dto;

import java.sql.Timestamp;

public class LogEventoResponse {
    private Integer id;
    private Integer usuarioId;
    private String accion;
    private Timestamp fecha;

    public LogEventoResponse() {
    }

    public LogEventoResponse(Integer id, Integer usuarioId, String accion, Timestamp fecha) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.accion = accion;
        this.fecha = fecha;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public Timestamp getFecha() {
        return fecha;
    }

    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }
}

