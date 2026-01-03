package com.miroma.miroma.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "presupuestos")
public class Presupuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "pareja_id", nullable = false)
    private Integer parejaId;

    @Column(name = "periodo", nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private String periodo;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Timestamp fechaCreacion;

    public Presupuesto() {
    }

    public Presupuesto(Integer parejaId, String periodo) {
        this.parejaId = parejaId;
        this.periodo = periodo;
        this.fechaCreacion = new Timestamp(System.currentTimeMillis());
    }

    @PrePersist
    protected void onCreate() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = new Timestamp(System.currentTimeMillis());
        }
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

