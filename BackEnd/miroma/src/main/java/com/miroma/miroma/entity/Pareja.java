package com.miroma.miroma.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "parejas")
public class Pareja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 400)
    private String nombre;

    @Column(name = "esposo_id", nullable = false)
    private Integer esposoId;

    @Column(name = "esposa_id", nullable = false)
    private Integer esposaId;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Timestamp fechaCreacion;

    public Pareja() {
    }

    public Pareja(String nombre, Integer esposoId, Integer esposaId) {
        this.nombre = nombre;
        this.esposoId = esposoId;
        this.esposaId = esposaId;
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getEsposoId() {
        return esposoId;
    }

    public void setEsposoId(Integer esposoId) {
        this.esposoId = esposoId;
    }

    public Integer getEsposaId() {
        return esposaId;
    }

    public void setEsposaId(Integer esposaId) {
        this.esposaId = esposaId;
    }

    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}

