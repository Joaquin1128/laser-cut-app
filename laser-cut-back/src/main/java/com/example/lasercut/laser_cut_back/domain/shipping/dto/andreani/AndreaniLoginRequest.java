package com.example.lasercut.laser_cut_back.domain.shipping.dto.andreani;

/**
 * DTO para el request de login con Andreani
 * TODO: Ajustar según la estructura real del endpoint de Login
 */
public class AndreaniLoginRequest {
    private String username;
    private String password;

    public AndreaniLoginRequest() {
    }

    public AndreaniLoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
