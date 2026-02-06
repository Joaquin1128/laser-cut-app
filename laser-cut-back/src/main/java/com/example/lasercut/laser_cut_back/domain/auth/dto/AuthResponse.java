package com.example.lasercut.laser_cut_back.domain.auth.dto;

/**
 * DTO para respuesta de autenticación
 */
public class AuthResponse {

    private String token;
    private String tipo;
    private UserInfo usuario;

    public AuthResponse() {
    }

    public AuthResponse(String token, String tipo, UserInfo usuario) {
        this.token = token;
        this.tipo = tipo;
        this.usuario = usuario;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public UserInfo getUsuario() {
        return usuario;
    }

    public void setUsuario(UserInfo usuario) {
        this.usuario = usuario;
    }

    public static class UserInfo {

        private Long id;
        private String nombre;
        private String email;
        private String role;

        public UserInfo() {
        }

        public UserInfo(Long id, String nombre, String email) {
            this.id = id;
            this.nombre = nombre;
            this.email = email;
        }

        public UserInfo(Long id, String nombre, String email, String role) {
            this.id = id;
            this.nombre = nombre;
            this.email = email;
            this.role = role;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
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

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
    
}
