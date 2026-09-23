package com.example.lasercut.laser_cut_back.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para la autenticación con Google Sign-In
 */
public class GoogleLoginRequest {

    @NotBlank(message = "El ID Token de Google es obligatorio")
    private String idToken;

    public GoogleLoginRequest() {
    }

    public GoogleLoginRequest(String idToken) {
        this.idToken = idToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

}
