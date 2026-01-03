package com.miroma.miroma.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "intentos_login")
public class IntentoLogin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "email", nullable = false, length = 255, unique = true)
    private String email;

    @Column(name = "intentos_fallidos", nullable = false)
    private Integer intentosFallidos = 0;

    @Column(name = "ultimo_intento")
    private Timestamp ultimoIntento;

    @Column(name = "bloqueado_hasta")
    private Timestamp bloqueadoHasta;

    @Column(name = "nivel_bloqueo", nullable = false)
    private Integer nivelBloqueo = 0;

    public IntentoLogin() {
    }

    public IntentoLogin(String email) {
        this.email = email;
        this.intentosFallidos = 0;
        this.nivelBloqueo = 0;
    }

    @PrePersist
    protected void onCreate() {
        if (this.intentosFallidos == null) {
            this.intentosFallidos = 0;
        }
        if (this.nivelBloqueo == null) {
            this.nivelBloqueo = 0;
        }
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
}

