package com.miroma.miroma.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class EgresoResponse {
    private Integer id;
    private Integer parejaId;
    private BigDecimal montoTotal;
    private String fecha;
    private String descripcion;
    private Integer categoriaId;
    private String categoriaNombre;
    private String categoriaDescripcion;
    private Integer periodoId;
    private String periodoNombre;
    private Timestamp fechaCreacion;

    public EgresoResponse() {
    }

    public EgresoResponse(Integer id, Integer parejaId, BigDecimal montoTotal, String fecha, 
                         String descripcion, Integer categoriaId, String categoriaNombre, 
                         String categoriaDescripcion, Timestamp fechaCreacion) {
        this.id = id;
        this.parejaId = parejaId;
        this.montoTotal = montoTotal;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.categoriaId = categoriaId;
        this.categoriaNombre = categoriaNombre;
        this.categoriaDescripcion = categoriaDescripcion;
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

    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Integer getPeriodoId() {
        return periodoId;
    }

    public void setPeriodoId(Integer periodoId) {
        this.periodoId = periodoId;
    }

    public String getPeriodoNombre() {
        return periodoNombre;
    }

    public void setPeriodoNombre(String periodoNombre) {
        this.periodoNombre = periodoNombre;
    }
}

