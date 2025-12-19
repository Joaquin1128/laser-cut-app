package com.example.lasercut.laser_cut_back.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.lasercut.laser_cut_back.dto.AuthResponse;
import com.example.lasercut.laser_cut_back.dto.LoginRequest;
import com.example.lasercut.laser_cut_back.dto.RegisterRequest;
import com.example.lasercut.laser_cut_back.exception.BadRequestException;
import com.example.lasercut.laser_cut_back.model.AppUser;
import com.example.lasercut.laser_cut_back.repository.UserRepository;

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
                usuario.getEmail()
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
                usuario.getEmail()
        );

        return new AuthResponse(token, "Bearer", userInfo);
    }

    public AuthResponse.UserInfo getCurrentUser(String email) {
        AppUser usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));

        return new AuthResponse.UserInfo(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail()
        );
    }

}
