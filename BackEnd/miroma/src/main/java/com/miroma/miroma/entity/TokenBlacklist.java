package com.miroma.miroma.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "token_blacklist")
public class TokenBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    private String tokenHash;

    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @Column(name = "fecha_invalidacion", nullable = false, updatable = false)
    private Timestamp fechaInvalidacion;

    @Column(name = "fecha_expiracion", nullable = false)
    private Timestamp fechaExpiracion;

    public TokenBlacklist() {
    }

    public TokenBlacklist(String tokenHash, Integer usuarioId, Timestamp fechaExpiracion) {
        this.tokenHash = tokenHash;
        this.usuarioId = usuarioId;
        this.fechaExpiracion = fechaExpiracion;
        this.fechaInvalidacion = new Timestamp(System.currentTimeMillis());
    }

    @PrePersist
    protected void onCreate() {
        if (this.fechaInvalidacion == null) {
            this.fechaInvalidacion = new Timestamp(System.currentTimeMillis());
        }
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Timestamp getFechaInvalidacion() {
        return fechaInvalidacion;
    }

    public void setFechaInvalidacion(Timestamp fechaInvalidacion) {
        this.fechaInvalidacion = fechaInvalidacion;
    }

    public Timestamp getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(Timestamp fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }
}


