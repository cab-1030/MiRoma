package com.miroma.miroma.dto;

public class CheckUserResponse {
    private boolean exists;
    private Integer userId;
    private String nombre;
    private String email;
    private String mensaje;

    public CheckUserResponse() {
    }

    public CheckUserResponse(boolean exists, String mensaje) {
        this.exists = exists;
        this.mensaje = mensaje;
    }

    // Getters y Setters
    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}

