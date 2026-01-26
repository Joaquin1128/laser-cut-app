package com.example.lasercut.laser_cut_back.domain.shipping.dto.andreani;

/**
 * DTO para la respuesta del login con Andreani
 * TODO: Ajustar según la estructura real de la respuesta
 */
public class AndreaniLoginResponse {
    private String accessToken;
    private String token; // Alternativa si el campo se llama diferente
    private String access_token; // Otra alternativa
    private Long expiresIn;
    private String tokenType;

    public AndreaniLoginResponse() {
    }

    public String getAccessToken() {
        if (accessToken != null) return accessToken;
        if (token != null) return token;
        if (access_token != null) return access_token;
        return null;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}
