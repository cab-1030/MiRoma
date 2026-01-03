package com.miroma.miroma.dto;

public class LinkPartnerResponse {
    private String mensaje;
    private Integer userId;
    private String partnerEmail;
    private String partnerNombre;

    public LinkPartnerResponse() {
    }

    public LinkPartnerResponse(String mensaje) {
        this.mensaje = mensaje;
    }

    // Getters y Setters
    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getPartnerEmail() {
        return partnerEmail;
    }

    public void setPartnerEmail(String partnerEmail) {
        this.partnerEmail = partnerEmail;
    }

    public String getPartnerNombre() {
        return partnerNombre;
    }

    public void setPartnerNombre(String partnerNombre) {
        this.partnerNombre = partnerNombre;
    }
}

