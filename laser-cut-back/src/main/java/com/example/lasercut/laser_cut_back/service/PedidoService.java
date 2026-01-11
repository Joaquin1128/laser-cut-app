package com.example.lasercut.laser_cut_back.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.lasercut.laser_cut_back.dto.CreatePedidoRequest;
import com.example.lasercut.laser_cut_back.dto.PedidoItemRequest;
import com.example.lasercut.laser_cut_back.dto.PedidoResponse;
import com.example.lasercut.laser_cut_back.exception.BadRequestException;
import com.example.lasercut.laser_cut_back.model.AppUser;
import com.example.lasercut.laser_cut_back.model.Pedido;
import com.example.lasercut.laser_cut_back.model.PedidoItem;
import com.example.lasercut.laser_cut_back.repository.PedidoRepository;
import com.example.lasercut.laser_cut_back.repository.UserRepository;

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

        // Validar que el pedido tenga items
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("El pedido debe contener al menos un item");
        }

        // Crear el pedido con el precio total
        Pedido pedido = new Pedido(usuario, request.getTotalPrice());

        // Crear y agregar cada item al pedido
        for (PedidoItemRequest itemRequest : request.getItems()) {
            PedidoItem item = new PedidoItem(
                    pedido,
                    itemRequest.getMaterial(),
                    itemRequest.getThickness(),
                    itemRequest.getQuantity(),
                    itemRequest.getUnitPrice(),
                    itemRequest.getTotalPrice()
            );

            if (itemRequest.getMetadata() != null) {
                item.setMetadata(itemRequest.getMetadata());
            }

            pedido.addItem(item);
        }

        // Guardar el pedido (los items se guardarán en cascada)
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

    public Pedido obtenerPedidoEntity(Long pedidoId) {
        return pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new BadRequestException("Pedido no encontrado"));
    }

    @Transactional
    public void actualizarPreferenceId(Long pedidoId, String preferenceId) {
        Pedido pedido = obtenerPedidoEntity(pedidoId);
        pedido.setMercadoPagoPreferenceId(preferenceId);
        pedidoRepository.save(pedido);
    }

    public Pedido obtenerPedidoPorPreferenceId(String preferenceId) {
        return pedidoRepository.findByMercadoPagoPreferenceId(preferenceId)
                .orElseThrow(() -> new BadRequestException("Pedido no encontrado con preferenceId: " + preferenceId));
    }

    @Transactional
    public void actualizarEstadoPago(Long pedidoId, String paymentId, String paymentStatus) {
        Pedido pedido = obtenerPedidoEntity(pedidoId);
        pedido.setMercadoPagoPaymentId(paymentId);
        
        // Mapear estado de MP a PaymentStatus
        Pedido.PaymentStatus status = mapearEstadoPago(paymentStatus);
        pedido.setPaymentStatus(status);
        
        // Actualizar estado del pedido según el pago
        if (status == Pedido.PaymentStatus.APPROVED) {
            pedido.setStatus(Pedido.OrderStatus.EN_PROCESO);
        } else if (status == Pedido.PaymentStatus.REJECTED || status == Pedido.PaymentStatus.CANCELLED) {
            pedido.setStatus(Pedido.OrderStatus.CANCELADO);
        }
        
        pedidoRepository.save(pedido);
    }

    private Pedido.PaymentStatus mapearEstadoPago(String mpStatus) {
        if (mpStatus == null) {
            return Pedido.PaymentStatus.PENDING;
        }
        
        switch (mpStatus.toLowerCase()) {
            case "approved":
                return Pedido.PaymentStatus.APPROVED;
            case "rejected":
                return Pedido.PaymentStatus.REJECTED;
            case "cancelled":
                return Pedido.PaymentStatus.CANCELLED;
            case "refunded":
                return Pedido.PaymentStatus.REFUNDED;
            default:
                return Pedido.PaymentStatus.PENDING;
        }
    }
    
}
