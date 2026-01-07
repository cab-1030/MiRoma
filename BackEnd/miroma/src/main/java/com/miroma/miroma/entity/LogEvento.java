package com.miroma.miroma.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "log_eventos")
public class LogEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @Column(name = "accion", nullable = false, length = 1000)
    private String accion;

    @Column(name = "fecha", nullable = false, updatable = false)
    private Timestamp fecha;

    public LogEvento() {
    }

    public LogEvento(Integer usuarioId, String accion) {
        this.usuarioId = usuarioId;
        this.accion = accion;
        this.fecha = new Timestamp(System.currentTimeMillis());
    }

    @PrePersist
    protected void onCreate() {
        if (this.fecha == null) {
            this.fecha = new Timestamp(System.currentTimeMillis());
        }
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

