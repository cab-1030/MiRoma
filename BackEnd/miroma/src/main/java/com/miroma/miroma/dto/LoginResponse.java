package com.miroma.miroma.dto;

public class LoginResponse {
    private String token;
    private String refreshToken;
    private String type = "Bearer";
    private Integer id;
    private String nombre;
    private String email;
    private String mensaje;
    private String parejaId;
    private Boolean hasPartner;

    public LoginResponse() {
    }

    public LoginResponse(String token, Integer id, String nombre, String email, String mensaje) {
        this.token = token;
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.mensaje = mensaje;
    }

    // Getters y Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

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

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getParejaId() {
        return parejaId;
    }

    public void setParejaId(String parejaId) {
        this.parejaId = parejaId;
    }

    public Boolean getHasPartner() {
        return hasPartner;
    }

    public void setHasPartner(Boolean hasPartner) {
        this.hasPartner = hasPartner;
    }
}

