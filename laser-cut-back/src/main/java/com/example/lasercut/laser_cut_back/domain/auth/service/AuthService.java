package com.example.lasercut.laser_cut_back.domain.auth.service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.lasercut.laser_cut_back.domain.auth.dto.AuthResponse;
import com.example.lasercut.laser_cut_back.domain.auth.dto.GoogleLoginRequest;
import com.example.lasercut.laser_cut_back.domain.auth.dto.LoginRequest;
import com.example.lasercut.laser_cut_back.domain.auth.dto.RegisterRequest;
import com.example.lasercut.laser_cut_back.domain.auth.model.AppUser;
import com.example.lasercut.laser_cut_back.domain.auth.model.UserRole;
import com.example.lasercut.laser_cut_back.domain.auth.repository.UserRepository;
import com.example.lasercut.laser_cut_back.exception.BadRequestException;
import com.example.lasercut.laser_cut_back.shared.service.JwtService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

/**
 * Servicio de autenticación
 * 
 * PREPARACIÓN FUTURA:
 * - Cuando se implemente el sistema de pedidos, aquí se podrá obtener el historial del usuario
 * - Se agregará método: getPedidosByUsuario(Long userId)
 * 
 * INTEGRACIÓN MERCADO PAGO:
 * - Cuando se integre MP, aquí se creará el customer en MP durante el registro
 * - Se guardará el customer_id de MP en el usuario
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Value("${google.client.id:}")
    private String googleClientId;

    private GoogleIdTokenVerifier googleIdTokenVerifier;

    public void setGoogleIdTokenVerifier(GoogleIdTokenVerifier verifier) {
        this.googleIdTokenVerifier = verifier;
    }

    private synchronized GoogleIdTokenVerifier getGoogleIdTokenVerifier() {
        if (googleIdTokenVerifier == null) {
            try {
                googleIdTokenVerifier = new GoogleIdTokenVerifier.Builder(
                        GoogleNetHttpTransport.newTrustedTransport(),
                        GsonFactory.getDefaultInstance())
                        .setAudience(Collections.singletonList(googleClientId))
                        .build();
            } catch (Exception e) {
                throw new RuntimeException("Error al inicializar verificador de Google: " + e.getMessage(), e);
            }
        }
        return googleIdTokenVerifier;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Validar que las contraseñas coincidan
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Las contraseñas no coinciden");
        }

        // Verificar si el email ya existe
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("El email ya está registrado");
        }

        // Crear nuevo usuario
        AppUser usuario = new AppUser();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRole(UserRole.USER);

        // TODO: INTEGRACIÓN MERCADO PAGO
        // Aquí se creará el customer en Mercado Pago:
        // MercadoPagoService.createCustomer(usuario.getEmail(), usuario.getNombre());
        // usuario.setMercadoPagoCustomerId(customerId);

        usuario = userRepository.save(usuario);

        // Generar token JWT
        String token = jwtService.generateToken(usuario.getEmail(), usuario.getId());

        // Crear respuesta
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRole().name()
        );

        return new AuthResponse(token, "Bearer", userInfo);
    }

    public AuthResponse login(LoginRequest request) {
        // Buscar usuario por email
        AppUser usuario = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Credenciales inválidas"));

        // Verificar contraseña
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new BadRequestException("Credenciales inválidas");
        }

        // Generar token JWT
        String token = jwtService.generateToken(usuario.getEmail(), usuario.getId());

        // Crear respuesta
        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRole().name()
        );

        return new AuthResponse(token, "Bearer", userInfo);
    }

    public AuthResponse.UserInfo getCurrentUser(String email) {
        AppUser usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));

        return new AuthResponse.UserInfo(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRole().name()
        );
    }

    @Transactional
    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        if (request == null || request.getIdToken() == null || request.getIdToken().trim().isEmpty()) {
            throw new BadRequestException("El ID Token de Google es obligatorio");
        }

        GoogleIdToken idToken;
        try {
            idToken = getGoogleIdTokenVerifier().verify(request.getIdToken());
        } catch (Exception e) {
            throw new BadRequestException("Error al verificar token con Google: " + e.getMessage());
        }

        if (idToken == null) {
            throw new BadRequestException("Token de Google inválido o expirado");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String pictureUrl = (String) payload.get("picture");

        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("No se pudo obtener el email de la cuenta de Google");
        }

        // Buscar si ya existe el usuario por email
        AppUser usuario = userRepository.findByEmail(email).orElse(null);

        if (usuario == null) {
            usuario = new AppUser();
            usuario.setEmail(email);
            usuario.setNombre(name != null && !name.trim().isEmpty() ? name : email.split("@")[0]);
            usuario.setAuthProvider("GOOGLE");
            usuario.setPictureUrl(pictureUrl);
            usuario.setRole(UserRole.USER);
            usuario = userRepository.save(usuario);
        } else {
            // Actualizar foto o nombre si no estaban presentes
            boolean modificado = false;
            if (pictureUrl != null && usuario.getPictureUrl() == null) {
                usuario.setPictureUrl(pictureUrl);
                modificado = true;
            }
            if ((usuario.getNombre() == null || usuario.getNombre().isEmpty()) && name != null) {
                usuario.setNombre(name);
                modificado = true;
            }
            if (modificado) {
                usuario = userRepository.save(usuario);
            }
        }

        // Generar token JWT propio
        String token = jwtService.generateToken(usuario.getEmail(), usuario.getId());

        AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRole().name()
        );

        return new AuthResponse(token, "Bearer", userInfo);
    }

}
