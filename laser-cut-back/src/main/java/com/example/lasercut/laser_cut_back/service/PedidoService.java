package com.example.lasercut.laser_cut_back.service;

import com.example.lasercut.laser_cut_back.dto.CreatePedidoRequest;
import com.example.lasercut.laser_cut_back.dto.PedidoResponse;
import com.example.lasercut.laser_cut_back.exception.BadRequestException;
import com.example.lasercut.laser_cut_back.model.AppUser;
import com.example.lasercut.laser_cut_back.model.Pedido;
import com.example.lasercut.laser_cut_back.repository.PedidoRepository;
import com.example.lasercut.laser_cut_back.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de pedidos
 * 
 * PREPARACIÓN INTEGRACIÓN MERCADO PAGO:
 * - Cuando se integre MP, aquí se creará la preferencia de pago
 * - Método futuro: crearPreferenciaPago(Long pedidoId)
 * - Método futuro: procesarPagoWebhook(String paymentId, String status)
 * - Actualizar estado del pedido según el estado del pago de MP
 */
@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public PedidoResponse crearPedido(Long userId, CreatePedidoRequest request) {
        AppUser usuario = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));

        Pedido pedido = new Pedido(
                usuario,
                request.getMaterial(),
                request.getThickness(),
                request.getQuantity(),
                request.getTotalPrice()
        );

        if (request.getMetadata() != null) {
            pedido.setMetadata(request.getMetadata());
        }

        pedido = pedidoRepository.save(pedido);

        // TODO: INTEGRACIÓN MERCADO PAGO
        // Aquí se creará la preferencia de pago:
        // String preferenceId = mercadoPagoService.crearPreferencia(pedido);
        // pedido.setMercadoPagoPreferenceId(preferenceId);
        // pedidoRepository.save(pedido);

        return new PedidoResponse(pedido);
    }

    public List<PedidoResponse> obtenerPedidosPorUsuario(Long userId) {
        List<Pedido> pedidos = pedidoRepository.findByUsuarioIdOrderByCreatedAtDesc(userId);
        return pedidos.stream()
                .map(PedidoResponse::new)
                .collect(Collectors.toList());
    }

    public PedidoResponse obtenerPedidoPorId(Long pedidoId, Long userId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new BadRequestException("Pedido no encontrado"));

        // Validar que el pedido pertenezca al usuario
        if (!pedido.getUsuario().getId().equals(userId)) {
            throw new BadRequestException("No tienes permiso para acceder a este pedido");
        }

        return new PedidoResponse(pedido);
    }
    
}
