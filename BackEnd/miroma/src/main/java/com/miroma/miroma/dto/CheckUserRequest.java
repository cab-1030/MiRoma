package com.miroma.miroma.dto;

import jakarta.validation.constraints.NotBlank;

public class CheckUserRequest {
    
    @NotBlank(message = "El email es obligatorio")
    private String email;

    public CheckUserRequest() {
    }

    public CheckUserRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

