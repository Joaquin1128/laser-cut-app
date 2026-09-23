package com.example.lasercut.laser_cut_back;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.lasercut.laser_cut_back.domain.auth.dto.AuthResponse;
import com.example.lasercut.laser_cut_back.domain.auth.dto.GoogleLoginRequest;
import com.example.lasercut.laser_cut_back.domain.auth.model.AppUser;
import com.example.lasercut.laser_cut_back.domain.auth.model.UserRole;
import com.example.lasercut.laser_cut_back.domain.auth.repository.UserRepository;
import com.example.lasercut.laser_cut_back.domain.auth.service.AuthService;
import com.example.lasercut.laser_cut_back.exception.BadRequestException;
import com.example.lasercut.laser_cut_back.shared.service.JwtService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

public class GoogleAuthServiceTest {

    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private GoogleIdTokenVerifier googleIdTokenVerifier;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService();
        ReflectionTestUtils.setField(authService, "userRepository", userRepository);
        ReflectionTestUtils.setField(authService, "jwtService", jwtService);
        ReflectionTestUtils.setField(authService, "googleClientId", "test-client-id");
        authService.setGoogleIdTokenVerifier(googleIdTokenVerifier);
    }

    @Test
    public void testLoginWithGoogleNuevoUsuario() throws Exception {
        String tokenStr = "valid-google-id-token";
        GoogleIdToken mockIdToken = mock(GoogleIdToken.class);
        GoogleIdToken.Payload mockPayload = new GoogleIdToken.Payload();
        mockPayload.setEmail("nuevo.google@test.com");
        mockPayload.set("name", "Usuario Nuevo");
        mockPayload.set("picture", "https://avatar.google.com/pic.jpg");

        when(googleIdTokenVerifier.verify(tokenStr)).thenReturn(mockIdToken);
        when(mockIdToken.getPayload()).thenReturn(mockPayload);
        when(userRepository.findByEmail("nuevo.google@test.com")).thenReturn(Optional.empty());

        AppUser savedUser = new AppUser();
        savedUser.setId(100L);
        savedUser.setEmail("nuevo.google@test.com");
        savedUser.setNombre("Usuario Nuevo");
        savedUser.setAuthProvider("GOOGLE");
        savedUser.setPictureUrl("https://avatar.google.com/pic.jpg");
        savedUser.setRole(UserRole.USER);

        when(userRepository.save(any(AppUser.class))).thenReturn(savedUser);
        when(jwtService.generateToken("nuevo.google@test.com", 100L)).thenReturn("jwt-token-test");

        GoogleLoginRequest request = new GoogleLoginRequest(tokenStr);
        AuthResponse response = authService.loginWithGoogle(request);

        assertNotNull(response);
        assertEquals("jwt-token-test", response.getToken());
        assertEquals("Bearer", response.getTipo());
        assertNotNull(response.getUsuario());
        assertEquals(100L, response.getUsuario().getId());
        assertEquals("Usuario Nuevo", response.getUsuario().getNombre());
        assertEquals("nuevo.google@test.com", response.getUsuario().getEmail());
        assertEquals("USER", response.getUsuario().getRole());

        verify(userRepository).save(any(AppUser.class));
    }

    @Test
    public void testLoginWithGoogleUsuarioExistente() throws Exception {
        String tokenStr = "valid-token-existing";
        GoogleIdToken mockIdToken = mock(GoogleIdToken.class);
        GoogleIdToken.Payload mockPayload = new GoogleIdToken.Payload();
        mockPayload.setEmail("existente@test.com");
        mockPayload.set("name", "Usuario Existente");
        mockPayload.set("picture", "https://avatar.google.com/pic2.jpg");

        when(googleIdTokenVerifier.verify(tokenStr)).thenReturn(mockIdToken);
        when(mockIdToken.getPayload()).thenReturn(mockPayload);

        AppUser existingUser = new AppUser();
        existingUser.setId(50L);
        existingUser.setEmail("existente@test.com");
        existingUser.setNombre("Usuario Existente");
        existingUser.setPictureUrl("https://avatar.google.com/pic2.jpg");
        existingUser.setRole(UserRole.USER);

        when(userRepository.findByEmail("existente@test.com")).thenReturn(Optional.of(existingUser));
        when(jwtService.generateToken("existente@test.com", 50L)).thenReturn("jwt-existing");

        GoogleLoginRequest request = new GoogleLoginRequest(tokenStr);
        AuthResponse response = authService.loginWithGoogle(request);

        assertNotNull(response);
        assertEquals("jwt-existing", response.getToken());
        assertEquals(50L, response.getUsuario().getId());
    }

    @Test
    public void testLoginWithGoogleTokenInvalido() throws Exception {
        String tokenStr = "invalid-token";
        when(googleIdTokenVerifier.verify(tokenStr)).thenReturn(null);

        GoogleLoginRequest request = new GoogleLoginRequest(tokenStr);
        assertThrows(BadRequestException.class, () -> authService.loginWithGoogle(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    public void testLoginWithGoogleTokenVacio() {
        GoogleLoginRequest request = new GoogleLoginRequest("");
        assertThrows(BadRequestException.class, () -> authService.loginWithGoogle(request));
    }

}
