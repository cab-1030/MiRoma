package com.miroma.miroma.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
@org.hibernate.annotations.DynamicInsert
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "pareja_id", nullable = true)
    private Integer parejaId;

    @Column(name = "rol_id", nullable = false)
    private Integer rolId;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "fecha_nacimiento", nullable = false)
    private java.sql.Date fechaNacimiento;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "activo", nullable = false)
    private Integer activo = 1;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private java.sql.Timestamp fechaCreacion;

    @Column(name = "token_version", nullable = false)
    private Integer tokenVersion = 1;

    // Constructores
    public Usuario() {
        this.fechaCreacion = new java.sql.Timestamp(System.currentTimeMillis());
        this.activo = 1;
        this.tokenVersion = 1;
    }

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = new java.sql.Timestamp(System.currentTimeMillis());
        }
        if (activo == null) {
            activo = 1;
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

    public Integer getRolId() {
        return rolId;
    }

    public void setRolId(Integer rolId) {
        this.rolId = rolId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public java.sql.Date getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(java.sql.Date fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getActivo() {
        return activo;
    }

    public void setActivo(Integer activo) {
        this.activo = activo;
    }

    public java.sql.Timestamp getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(java.sql.Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Integer getTokenVersion() {
        return tokenVersion;
    }

    public void setTokenVersion(Integer tokenVersion) {
        this.tokenVersion = tokenVersion;
    }
}

