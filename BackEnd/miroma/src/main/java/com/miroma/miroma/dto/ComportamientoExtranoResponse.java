package com.miroma.miroma.dto;

import java.sql.Timestamp;

public class ComportamientoExtranoResponse {
    private Integer id;
    private String email;
    private Integer intentosFallidos;
    private Timestamp ultimoIntento;
    private Timestamp bloqueadoHasta;
    private Integer nivelBloqueo;
    private String tipoComportamiento;
    private String descripcion;

    public ComportamientoExtranoResponse() {
    }

    public ComportamientoExtranoResponse(Integer id, String email, Integer intentosFallidos, 
                                         Timestamp ultimoIntento, Timestamp bloqueadoHasta, 
                                         Integer nivelBloqueo, String tipoComportamiento, String descripcion) {
        this.id = id;
        this.email = email;
        this.intentosFallidos = intentosFallidos;
        this.ultimoIntento = ultimoIntento;
        this.bloqueadoHasta = bloqueadoHasta;
        this.nivelBloqueo = nivelBloqueo;
        this.tipoComportamiento = tipoComportamiento;
        this.descripcion = descripcion;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getIntentosFallidos() {
        return intentosFallidos;
    }

    public void setIntentosFallidos(Integer intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }

    public Timestamp getUltimoIntento() {
        return ultimoIntento;
    }

    public void setUltimoIntento(Timestamp ultimoIntento) {
        this.ultimoIntento = ultimoIntento;
    }

    public Timestamp getBloqueadoHasta() {
        return bloqueadoHasta;
    }

    public void setBloqueadoHasta(Timestamp bloqueadoHasta) {
        this.bloqueadoHasta = bloqueadoHasta;
    }

    public Integer getNivelBloqueo() {
        return nivelBloqueo;
    }

    public void setNivelBloqueo(Integer nivelBloqueo) {
        this.nivelBloqueo = nivelBloqueo;
    }

    public String getTipoComportamiento() {
        return tipoComportamiento;
    }

    public void setTipoComportamiento(String tipoComportamiento) {
        this.tipoComportamiento = tipoComportamiento;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}


