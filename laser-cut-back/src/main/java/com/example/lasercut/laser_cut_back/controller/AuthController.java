package com.example.lasercut.laser_cut_back.controller;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.lasercut.laser_cut_back.dto.AuthResponse;
import com.example.lasercut.laser_cut_back.dto.LoginRequest;
import com.example.lasercut.laser_cut_back.dto.RegisterRequest;
import com.example.lasercut.laser_cut_back.service.AuthService;

/**
 * Controlador de autenticación
 * 
 * Endpoints:
 * - POST /api/auth/register - Registro de nuevos usuarios
 * - POST /api/auth/login - Inicio de sesión
 * - GET /api/auth/me - Obtener información del usuario autenticado
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse.UserInfo> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        AuthResponse.UserInfo userInfo = authService.getCurrentUser(email);
        return ResponseEntity.ok(userInfo);
    }
    
}
