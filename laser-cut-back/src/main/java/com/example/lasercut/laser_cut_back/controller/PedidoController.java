package com.example.lasercut.laser_cut_back.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.lasercut.laser_cut_back.dto.CreatePedidoRequest;
import com.example.lasercut.laser_cut_back.dto.PedidoResponse;
import com.example.lasercut.laser_cut_back.model.AppUser;
import com.example.lasercut.laser_cut_back.repository.UserRepository;
import com.example.lasercut.laser_cut_back.service.PedidoService;

/**
 * Controlador de pedidos
 * 
 * PREPARACIÓN INTEGRACIÓN MERCADO PAGO:
 * - Cuando se integre MP, aquí se agregará:
 *   POST /api/orders/{id}/create-payment - Crear preferencia de pago
 *   POST /api/orders/webhook - Recibir webhook de MP
 *   GET /api/orders/{id}/payment-status - Consultar estado de pago
 */
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<PedidoResponse> crearPedido(
            @Valid @RequestBody CreatePedidoRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        AppUser usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        PedidoResponse pedido = pedidoService.crearPedido(usuario.getId(), request);
        return ResponseEntity.ok(pedido);
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponse>> obtenerPedidos(Authentication authentication) {
        String email = authentication.getName();
        AppUser usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<PedidoResponse> pedidos = pedidoService.obtenerPedidosPorUsuario(usuario.getId());
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> obtenerPedido(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();
        AppUser usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        PedidoResponse pedido = pedidoService.obtenerPedidoPorId(id, usuario.getId());
        return ResponseEntity.ok(pedido);
    }
    
}
