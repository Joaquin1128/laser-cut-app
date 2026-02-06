package com.example.lasercut.laser_cut_back.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.lasercut.laser_cut_back.domain.auth.model.AppUser;
import com.example.lasercut.laser_cut_back.domain.auth.model.UserRole;
import com.example.lasercut.laser_cut_back.domain.auth.repository.UserRepository;

/**
 * Crea el usuario admin al iniciar la aplicación si está configurado en application.properties
 * y no existe ya un usuario con ese email.
 * 
 * Configuración opcional en application.properties:
 *   admin.email=admin@example.com
 *   admin.password=tu-password-seguro
 *   admin.name=Administrador
 */
@Component
public class AdminUserInitializer implements ApplicationRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${admin.email:}")
    private String adminEmail;

    @Value("${admin.password:}")
    private String adminPassword;

    @Value("${admin.name:Admin}")
    private String adminName;

    @Override
    public void run(ApplicationArguments args) {
        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            return;
        }

        if (userRepository.findByEmail(adminEmail).isPresent()) {
            return;
        }

        AppUser admin = new AppUser();
        admin.setNombre(adminName);
        admin.setEmail(adminEmail.trim());
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(UserRole.ADMIN);
        userRepository.save(admin);
    }
}
