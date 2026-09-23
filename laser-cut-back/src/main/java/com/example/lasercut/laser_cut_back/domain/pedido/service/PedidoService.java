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
import com.example.lasercut.laser_cut_back.domain.pedido.dto.PedidoWithCustomerResponse;
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

    @Autowired
    private com.example.lasercut.laser_cut_back.domain.archivo.service.DxfStorageService dxfStorageService;

    @Autowired
    private OrderEmailService orderEmailService;

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
            if (itemRequest.getArchivoId() != null) {
                item.setArchivoId(itemRequest.getArchivoId());
            }
            if (itemRequest.getArchivoNombre() != null) {
                item.setArchivoNombre(itemRequest.getArchivoNombre());
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

    /**
     * Lista todos los pedidos con datos del cliente. Solo para uso del admin.
     */
    public List<PedidoWithCustomerResponse> obtenerTodosPedidosParaAdmin() {
        List<Pedido> pedidos = pedidoRepository.findAllOrderByCreatedAtDesc();
        return pedidos.stream()
                .map(PedidoWithCustomerResponse::new)
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
            // Si el pago es aprobado, marcar como PAID
            pedido.setStatus(Pedido.OrderStatus.PAID);
        } else if (status == Pedido.PaymentStatus.REJECTED || status == Pedido.PaymentStatus.CANCELLED) {
            pedido.setStatus(Pedido.OrderStatus.CANCELADO);
        }
        
        pedido = pedidoRepository.save(pedido);

        if (status == Pedido.PaymentStatus.APPROVED) {
            if (orderEmailService != null) {
                orderEmailService.enviarFichaPedidoAdmin(pedido);
            }
        }
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
        pedido.setBillingPhone(request.getBillingPhone());

        String taxCondition = request.getTaxCondition() != null ? request.getTaxCondition().trim() : "CONSUMIDOR_FINAL";
        pedido.setTaxCondition(taxCondition);

        String billingType = request.getBillingType();
        if (billingType == null || billingType.isBlank()) {
            billingType = "RESPONSABLE_INSCRIPTO".equalsIgnoreCase(taxCondition) ? "A" : "B";
        }
        pedido.setBillingType(billingType.toUpperCase());

        String fiscalId = request.getFiscalId() != null ? request.getFiscalId().trim() : "";
        if ("RESPONSABLE_INSCRIPTO".equalsIgnoreCase(taxCondition) || "A".equalsIgnoreCase(billingType)) {
            if (!com.example.lasercut.laser_cut_back.shared.util.CuitValidator.isValidCuit(fiscalId)) {
                throw new BadRequestException("El CUIT ingresado no es válido para Factura A.");
            }
            pedido.setFiscalId(com.example.lasercut.laser_cut_back.shared.util.CuitValidator.cleanCuit(fiscalId));
        } else {
            String clean = fiscalId.replaceAll("[^0-9]", "");
            if (clean.length() < 7 || clean.length() > 11) {
                throw new BadRequestException("El DNI o CUIT debe tener entre 7 y 11 dígitos numéricos.");
            }
            pedido.setFiscalId(clean);
        }

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

    /**
     * Permite al administrador cambiar el estado de un pedido según la máquina de estados estricta.
     * Soporta confirmación de pagos por transferencia bancaria.
     */
    @Transactional
    public PedidoWithCustomerResponse cambiarEstadoAdmin(Long pedidoId, String nuevoEstadoStr, String paymentMethod, String motivo) {
        Pedido pedido = obtenerPedidoEntity(pedidoId);
        Pedido.OrderStatus estadoActual = pedido.getStatus();

        Pedido.OrderStatus nuevoEstado;
        try {
            nuevoEstado = Pedido.OrderStatus.valueOf(nuevoEstadoStr.toUpperCase().trim());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException("Estado de pedido inválido: " + nuevoEstadoStr);
        }

        if (estadoActual == nuevoEstado) {
            return new PedidoWithCustomerResponse(pedido);
        }

        // Validación estricta de transiciones
        switch (estadoActual) {
            case PENDING_CHECKOUT:
                if (nuevoEstado != Pedido.OrderStatus.CANCELADO) {
                    throw new BadRequestException("Un pedido en proceso de checkout solo puede ser cancelado.");
                }
                pedido.setPaymentStatus(Pedido.PaymentStatus.CANCELLED);
                break;

            case PENDING_PAYMENT:
            case PENDIENTE:
                if (nuevoEstado == Pedido.OrderStatus.PAID) {
                    pedido.setPaymentStatus(Pedido.PaymentStatus.APPROVED);
                    pedido.setPaymentMethod(paymentMethod != null && !paymentMethod.isBlank() ? paymentMethod : "TRANSFERENCIA");
                } else if (nuevoEstado == Pedido.OrderStatus.CANCELADO) {
                    pedido.setPaymentStatus(Pedido.PaymentStatus.CANCELLED);
                } else {
                    throw new BadRequestException("No se puede pasar a " + nuevoEstado + " sin haber confirmado el pago primero (estado PAID).");
                }
                break;

            case PAID:
                if (nuevoEstado == Pedido.OrderStatus.EN_PROCESO) {
                    // Mantiene PaymentStatus.APPROVED
                } else if (nuevoEstado == Pedido.OrderStatus.CANCELADO) {
                    pedido.setPaymentStatus(Pedido.PaymentStatus.REFUNDED);
                } else {
                    throw new BadRequestException("Desde PAID solo se puede avanzar a EN_PROCESO o CANCELADO.");
                }
                break;

            case EN_PROCESO:
                if (nuevoEstado == Pedido.OrderStatus.FINALIZADO) {
                    // Completado exitosamente
                } else if (nuevoEstado == Pedido.OrderStatus.CANCELADO) {
                    pedido.setPaymentStatus(Pedido.PaymentStatus.CANCELLED);
                } else {
                    throw new BadRequestException("Desde EN_PROCESO solo se puede avanzar a FINALIZADO o CANCELADO.");
                }
                break;

            case FINALIZADO:
                throw new BadRequestException("El pedido #" + pedidoId + " ya está FINALIZADO y no puede modificarse.");

            case CANCELADO:
                throw new BadRequestException("El pedido #" + pedidoId + " está CANCELADO y no puede modificarse.");

            default:
                throw new BadRequestException("Transición no soportada desde estado " + estadoActual);
        }

        pedido.setStatus(nuevoEstado);
        pedido = pedidoRepository.save(pedido);

        if (nuevoEstado == Pedido.OrderStatus.PAID) {
            if (orderEmailService != null) {
                orderEmailService.enviarFichaPedidoAdmin(pedido);
            }
        }

        return new PedidoWithCustomerResponse(pedido);
    }

    /**
     * Reenvía manualmente la ficha del pedido por email al admin.
     */
    public void reenviarFichaAdmin(Long pedidoId) {
        Pedido pedido = obtenerPedidoEntity(pedidoId);
        if (orderEmailService != null) {
            orderEmailService.enviarFichaPedidoAdmin(pedido);
        }
    }

    /**
     * Recupera el recurso DXF de un item del pedido para descarga.
     * Solo accesible para el usuario dueño del pedido o un usuario ADMIN.
     */
    public org.springframework.core.io.Resource obtenerRecursoDxfItem(Long pedidoId, Long itemId, AppUser usuario) {
        Pedido pedido = obtenerPedidoEntity(pedidoId);
        boolean isAdmin = usuario.getRole() == com.example.lasercut.laser_cut_back.domain.auth.model.UserRole.ADMIN;
        if (!isAdmin && !pedido.getUsuario().getId().equals(usuario.getId())) {
            throw new BadRequestException("No tienes permiso para acceder a este archivo");
        }

        PedidoItem item = pedido.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Item no encontrado en el pedido"));

        String archivoId = item.getArchivoId();
        if (archivoId == null && item.getMetadata() != null) {
            try {
                com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(item.getMetadata());
                if (node.has("archivoId")) {
                    archivoId = node.get("archivoId").asText();
                }
            } catch (Exception ignored) {}
        }

        if (archivoId == null || archivoId.isBlank()) {
            throw new BadRequestException("El item no tiene un archivo DXF registrado.");
        }

        return dxfStorageService.loadAsResource(archivoId);
    }

    /**
     * Retorna el nombre de archivo amigable para la descarga del DXF.
     */
    public String obtenerNombreDxfItem(Long pedidoId, Long itemId) {
        Pedido pedido = obtenerPedidoEntity(pedidoId);
        PedidoItem item = pedido.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Item no encontrado en el pedido"));

        if (item.getArchivoNombre() != null && !item.getArchivoNombre().isBlank()) {
            return item.getArchivoNombre();
        }
        if (item.getMetadata() != null) {
            try {
                com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(item.getMetadata());
                if (node.has("archivoNombre")) {
                    return node.get("archivoNombre").asText();
                }
            } catch (Exception ignored) {}
        }
        return "pieza_" + itemId + ".dxf";
    }

}
