package com.miroma.miroma.dto;

import jakarta.validation.constraints.NotBlank;

public class LinkPartnerRequest {
    
    @NotBlank(message = "El email de la pareja es obligatorio")
    private String partnerEmail;

    public LinkPartnerRequest() {
    }

    public LinkPartnerRequest(String partnerEmail) {
        this.partnerEmail = partnerEmail;
    }

    public String getPartnerEmail() {
        return partnerEmail;
    }

    public void setPartnerEmail(String partnerEmail) {
        this.partnerEmail = partnerEmail;
    }
}

