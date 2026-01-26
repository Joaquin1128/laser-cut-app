package com.example.lasercut.laser_cut_back.domain.shipping.provider;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Servicio de autenticación con Andreani
 * Maneja el login y la obtención de access tokens
 * Los tokens tienen vigencia de 24 horas según la documentación
 */
@Service
public class AndreaniAuthService {

    private final RestTemplate restTemplate;
    private String cachedToken;
    private LocalDateTime tokenExpiry;
    
    @Value("${andreani.api.url:https://apis.andreani.com}")
    private String apiUrl;
    
    @Value("${andreani.api.username:}")
    private String username;
    
    @Value("${andreani.api.password:}")
    private String password;

    public AndreaniAuthService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Obtiene un access token válido
     * Si hay un token en caché y aún no expiró, lo retorna
     * Si no, hace login para obtener uno nuevo
     */
    public String getAccessToken() {
        // Si hay un token válido en caché, retornarlo
        if (cachedToken != null && tokenExpiry != null && LocalDateTime.now().isBefore(tokenExpiry)) {
            return cachedToken;
        }

        // Hacer login para obtener nuevo token
        return login();
    }

    /**
     * Realiza el login con Andreani y obtiene un access token
     * Endpoint: POST /login (verificar URL exacta en documentación)
     */
    private String login() {
        try {
            // TODO: Verificar endpoint exacto de Login según documentación
            // Posibles: /login, /v2/login, /auth/login, etc.
            String loginEndpoint = apiUrl + "/login";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, String> loginRequest = new HashMap<>();
            loginRequest.put("username", username);
            loginRequest.put("password", password);
            
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(loginRequest, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                loginEndpoint,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            String token = extractTokenFromResponse(response.getBody());
            
            // Guardar token en caché con expiración (23 horas para estar seguro)
            cachedToken = token;
            tokenExpiry = LocalDateTime.now().plusHours(23);
            
            return token;
            
        } catch (Exception e) {
            throw new RuntimeException("Error al autenticar con Andreani: " + e.getMessage(), e);
        }
    }

    /**
     * Extrae el token de la respuesta del login
     */
    private String extractTokenFromResponse(Map<String, Object> responseBody) {
        if (responseBody != null) {
            Object token = responseBody.get("accessToken");
            if (token == null) {
                token = responseBody.get("token");
            }
            if (token == null) {
                token = responseBody.get("access_token");
            }
            if (token != null) {
                return token.toString();
            }
        }
        throw new RuntimeException("No se pudo extraer el token de la respuesta de Andreani");
    }

    /**
     * Invalida el token en caché (útil para forzar re-autenticación)
     */
    public void invalidateToken() {
        cachedToken = null;
        tokenExpiry = null;
    }
}
