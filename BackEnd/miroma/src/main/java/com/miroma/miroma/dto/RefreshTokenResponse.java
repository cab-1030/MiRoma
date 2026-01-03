package com.miroma.miroma.dto;

public class RefreshTokenResponse {
    private String token;
    private String refreshToken;
    private String type = "Bearer";
    private String mensaje;

    public RefreshTokenResponse() {
    }

    public RefreshTokenResponse(String token, String refreshToken, String mensaje) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.mensaje = mensaje;
    }

    // Getters y Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}

