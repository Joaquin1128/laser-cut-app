package com.example.lasercut.laser_cut_back.domain.pedido.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import com.example.lasercut.laser_cut_back.domain.pedido.dto.BillingDataRequest;
import com.example.lasercut.laser_cut_back.domain.pedido.dto.CreatePedidoRequest;
import com.example.lasercut.laser_cut_back.domain.pedido.dto.PedidoItemRequest;
import com.example.lasercut.laser_cut_back.domain.pedido.dto.PedidoResponse;
import com.example.lasercut.laser_cut_back.domain.pedido.dto.ShippingDataRequest;
import com.example.lasercut.laser_cut_back.domain.pedido.model.Pedido;
import com.example.lasercut.laser_cut_back.domain.pedido.model.PedidoItem;
import com.example.lasercut.laser_cut_back.domain.pedido.repository.PedidoRepository;
import com.example.lasercut.laser_cut_back.domain.auth.model.AppUser;
import com.example.lasercut.laser_cut_back.domain.auth.repository.UserRepository;
import com.example.lasercut.laser_cut_back.exception.BadRequestException;

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
            // Si el pago es aprobado, marcar como PAID primero, luego puede pasar a EN_PROCESO
            pedido.setStatus(Pedido.OrderStatus.PAID);
            // También mantener EN_PROCESO para compatibilidad con flujos anteriores
            // pedido.setStatus(Pedido.OrderStatus.EN_PROCESO);
        } else if (status == Pedido.PaymentStatus.REJECTED || status == Pedido.PaymentStatus.CANCELLED) {
            pedido.setStatus(Pedido.OrderStatus.CANCELADO);
        }
        
        pedidoRepository.save(pedido);
    }

    /**
     * Actualiza los datos de facturación del pedido
     */
    @Transactional
    public PedidoResponse actualizarFacturacion(Long pedidoId, Long userId, BillingDataRequest request) {
        Pedido pedido = obtenerPedidoEntity(pedidoId);

        // Validar que el pedido pertenezca al usuario
        if (!pedido.getUsuario().getId().equals(userId)) {
            throw new BadRequestException("No tienes permiso para acceder a este pedido");
        }

        // Validar que el pedido esté en un estado editable
        if (pedido.getStatus() != Pedido.OrderStatus.PENDING_CHECKOUT && 
            pedido.getStatus() != Pedido.OrderStatus.PENDING_PAYMENT) {
            throw new BadRequestException("No se puede actualizar la facturación de un pedido en estado " + pedido.getStatus());
        }

        // Actualizar datos de facturación
        pedido.setBillingName(request.getBillingName());
        pedido.setBillingEmail(request.getBillingEmail());
        pedido.setBillingType("C"); // Siempre tipo C
        pedido.setFiscalId(request.getFiscalId()); // DNI
        pedido.setBillingPhone(request.getBillingPhone()); // Teléfono

        pedido = pedidoRepository.save(pedido);
        return new PedidoResponse(pedido);
    }

    /**
     * Actualiza los datos de envío del pedido
     */
    @Transactional
    public PedidoResponse actualizarEnvio(Long pedidoId, Long userId, ShippingDataRequest request) {
        Pedido pedido = obtenerPedidoEntity(pedidoId);

        // Validar que el pedido pertenezca al usuario
        if (!pedido.getUsuario().getId().equals(userId)) {
            throw new BadRequestException("No tienes permiso para acceder a este pedido");
        }

        // Validar que el pedido esté en un estado editable
        if (pedido.getStatus() != Pedido.OrderStatus.PENDING_CHECKOUT && 
            pedido.getStatus() != Pedido.OrderStatus.PENDING_PAYMENT) {
            throw new BadRequestException("No se puede actualizar el envío de un pedido en estado " + pedido.getStatus());
        }

        // Actualizar tipo de envío - siempre DELIVERY (no hay retiro en fábrica)
        pedido.setShippingType(Pedido.ShippingType.DELIVERY);

        // Envío a domicilio: requiere dirección
        if (request.getStreet() == null || request.getStreet().isEmpty() ||
            request.getCity() == null || request.getCity().isEmpty() ||
            request.getPostalCode() == null || request.getPostalCode().isEmpty()) {
            throw new BadRequestException("Se requiere dirección completa para el envío");
        }

        pedido.setShippingAddressStreet(request.getStreet());
        pedido.setShippingAddressUnit(request.getUnit()); // Piso / Depto / Unidad (opcional)
        pedido.setShippingAddressCity(request.getCity());
        pedido.setShippingAddressPostalCode(request.getPostalCode());
        pedido.setShippingAddressProvince(request.getProvince());
        pedido.setShippingAddressCountry(request.getCountry() != null ? request.getCountry() : "Argentina");
        // El costo de envío se calculará por separado

        pedido = pedidoRepository.save(pedido);
        return new PedidoResponse(pedido);
    }

    /**
     * Actualiza el costo de envío del pedido
     */
    @Transactional
    public PedidoResponse actualizarCostoEnvio(Long pedidoId, Long userId, BigDecimal shippingCost) {
        Pedido pedido = obtenerPedidoEntity(pedidoId);

        // Validar que el pedido pertenezca al usuario
        if (!pedido.getUsuario().getId().equals(userId)) {
            throw new BadRequestException("No tienes permiso para acceder a este pedido");
        }

        if (shippingCost == null) {
            shippingCost = BigDecimal.ZERO;
        }

        pedido.setShippingCost(shippingCost);
        pedido = pedidoRepository.save(pedido);
        return new PedidoResponse(pedido);
    }

    /**
     * Cambia el estado del pedido a PENDING_PAYMENT (listo para pagar)
     */
    @Transactional
    public PedidoResponse prepararPago(Long pedidoId, Long userId) {
        Pedido pedido = obtenerPedidoEntity(pedidoId);

        // Validar que el pedido pertenezca al usuario
        if (!pedido.getUsuario().getId().equals(userId)) {
            throw new BadRequestException("No tienes permiso para acceder a este pedido");
        }

        // Validar que tenga facturación y envío completos
        if (pedido.getBillingName() == null || pedido.getBillingEmail() == null) {
            throw new BadRequestException("El pedido debe tener datos de facturación completos");
        }

        // Validar que tenga dirección completa (siempre es envío a domicilio)
        if (pedido.getShippingAddressStreet() == null || pedido.getShippingAddressCity() == null) {
            throw new BadRequestException("El pedido debe tener dirección completa");
        }

        // Cambiar estado a PENDING_PAYMENT
        pedido.setStatus(Pedido.OrderStatus.PENDING_PAYMENT);
        pedido = pedidoRepository.save(pedido);

        return new PedidoResponse(pedido);
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
