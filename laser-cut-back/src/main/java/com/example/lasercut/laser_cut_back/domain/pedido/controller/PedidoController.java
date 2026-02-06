package com.example.lasercut.laser_cut_back.domain.pedido.controller;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.lasercut.laser_cut_back.domain.pedido.dto.BillingDataRequest;
import com.example.lasercut.laser_cut_back.domain.pedido.dto.CreatePedidoRequest;
import com.example.lasercut.laser_cut_back.domain.pedido.dto.PedidoResponse;
import com.example.lasercut.laser_cut_back.domain.pedido.dto.PedidoWithCustomerResponse;
import com.example.lasercut.laser_cut_back.domain.pedido.dto.ShippingDataRequest;
import com.example.lasercut.laser_cut_back.domain.pedido.service.PedidoService;
import com.example.lasercut.laser_cut_back.domain.auth.model.AppUser;
import com.example.lasercut.laser_cut_back.domain.auth.model.UserRole;
import com.example.lasercut.laser_cut_back.domain.auth.repository.UserRepository;
import com.example.lasercut.laser_cut_back.domain.payment.dto.PreferenceResponse;
import com.example.lasercut.laser_cut_back.domain.payment.service.MercadoPagoService;
import com.example.lasercut.laser_cut_back.domain.shipping.dto.ShippingQuoteRequest;
import com.example.lasercut.laser_cut_back.domain.shipping.dto.ShippingQuoteResponse;
import com.example.lasercut.laser_cut_back.domain.shipping.service.ShippingService;

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
    private MercadoPagoService mercadoPagoService;

    @Autowired
    private ShippingService shippingService;

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

    /**
     * Lista todos los pedidos de todos los usuarios con información del cliente.
     * Solo accesible para usuarios con rol ADMIN.
     */
    @GetMapping("/admin/all")
    public ResponseEntity<List<PedidoWithCustomerResponse>> obtenerTodosPedidosAdmin(Authentication authentication) {
        String email = authentication.getName();
        AppUser usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getRole() != UserRole.ADMIN) {
            return ResponseEntity.status(403).build();
        }

        List<PedidoWithCustomerResponse> pedidos = pedidoService.obtenerTodosPedidosParaAdmin();
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
    
    @PostMapping("/{id}/create-preference")
    public ResponseEntity<PreferenceResponse> crearPreferenciaPago(
            @PathVariable Long id,
            @RequestBody(required = false) java.util.Map<String, String> urls,
            Authentication authentication
    ) {
        String email = authentication.getName();
        AppUser usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validar que el pedido pertenezca al usuario
        pedidoService.obtenerPedidoPorId(id, usuario.getId());
        var pedido = pedidoService.obtenerPedidoEntity(id);

        // URLs de retorno (por defecto)
        String successUrl = urls != null && urls.containsKey("successUrl") 
                ? urls.get("successUrl") 
                : "http://localhost:3000/payment/success";
        String failureUrl = urls != null && urls.containsKey("failureUrl") 
                ? urls.get("failureUrl") 
                : "http://localhost:3000/payment/failure";
        String pendingUrl = urls != null && urls.containsKey("pendingUrl") 
                ? urls.get("pendingUrl") 
                : "http://localhost:3000/payment/pending";

        // Crear preferencia en Mercado Pago
        PreferenceResponse preference = mercadoPagoService.crearPreferencia(
                pedido, successUrl, failureUrl, pendingUrl);

        // Guardar preference_id en el pedido
        pedidoService.actualizarPreferenceId(id, preference.getId());

        return ResponseEntity.ok(preference);
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> recibirWebhook(
            @RequestBody(required = false) String data,
            @RequestParam(value = "data.id", required = false) String paymentIdParam,
            @RequestParam(value = "type", required = false) String typeParam,
            @RequestHeader(value = "x-signature", required = false) String signature,
            @RequestHeader(value = "x-request-id", required = false) String requestId
    ) {
        try {
            String paymentId = null;
            
            // Intentar obtener paymentId del body (JSON) o de query params
            if (data != null && !data.isEmpty()) {
                paymentId = mercadoPagoService.procesarWebhook(data);
            } else if (paymentIdParam != null && "payment".equals(typeParam)) {
                paymentId = paymentIdParam;
            }
            
            if (paymentId != null && !paymentId.isEmpty()) {
                try {
                    // Obtener el pago completo para acceder a external_reference
                    com.mercadopago.client.payment.PaymentClient paymentClient = new com.mercadopago.client.payment.PaymentClient();
                    com.mercadopago.resources.payment.Payment payment = paymentClient.get(Long.parseLong(paymentId));
                    
                    // El external_reference contiene el ID del pedido
                    String externalReference = payment.getExternalReference();
                    if (externalReference != null && !externalReference.isEmpty()) {
                        Long pedidoId = Long.parseLong(externalReference);
                        String paymentStatus = payment.getStatus();
                        pedidoService.actualizarEstadoPago(pedidoId, paymentId, paymentStatus);
                    }
                } catch (Exception e) {
                    System.err.println("Error al procesar pago del webhook: " + e.getMessage());
                    e.printStackTrace();
                    // Continuar para retornar OK a MP
                }
            }
            
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            // Log del error pero retornar 200 para que MP no reintente
            System.err.println("Error procesando webhook: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok("OK");
        }
    }

    // ========== ENDPOINTS DE CHECKOUT ==========

    /**
     * Iniciar checkout - crea un pedido en estado PENDING_CHECKOUT
     * Este endpoint crea el pedido ANTES del pago, permitiendo que el usuario
     * complete facturación y envío antes de pagar.
     */
    @PostMapping("/checkout/start")
    public ResponseEntity<PedidoResponse> iniciarCheckout(
            @Valid @RequestBody CreatePedidoRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        AppUser usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Crear pedido en estado PENDING_CHECKOUT
        PedidoResponse pedido = pedidoService.crearPedido(usuario.getId(), request);
        return ResponseEntity.ok(pedido);
    }

    /**
     * Actualizar datos de facturación del pedido
     */
    @PutMapping("/checkout/{id}/billing")
    public ResponseEntity<PedidoResponse> actualizarFacturacion(
            @PathVariable Long id,
            @Valid @RequestBody BillingDataRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        AppUser usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        PedidoResponse pedido = pedidoService.actualizarFacturacion(id, usuario.getId(), request);
        return ResponseEntity.ok(pedido);
    }

    /**
     * Actualizar datos de envío del pedido
     */
    @PutMapping("/checkout/{id}/shipping")
    public ResponseEntity<PedidoResponse> actualizarEnvio(
            @PathVariable Long id,
            @Valid @RequestBody ShippingDataRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        AppUser usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        PedidoResponse pedido = pedidoService.actualizarEnvio(id, usuario.getId(), request);
        return ResponseEntity.ok(pedido);
    }

    /**
     * Calcular costo de envío (cotización)
     * Este endpoint calcula el costo de envío sin actualizar el pedido todavía
     */
    @PostMapping("/checkout/calculate-shipping")
    public ResponseEntity<ShippingQuoteResponse> calcularEnvio(
            @Valid @RequestBody ShippingQuoteRequest request
    ) {
        ShippingQuoteResponse quote = shippingService.calculateShipping(request);
        return ResponseEntity.ok(quote);
    }

    /**
     * Actualizar costo de envío del pedido
     * Se llama después de calcular el envío para guardarlo en el pedido
     */
    @PutMapping("/checkout/{id}/shipping-cost")
    public ResponseEntity<PedidoResponse> actualizarCostoEnvio(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        AppUser usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        BigDecimal shippingCost = request.containsKey("shippingCost") 
            ? new BigDecimal(request.get("shippingCost").toString())
            : BigDecimal.ZERO;

        PedidoResponse pedido = pedidoService.actualizarCostoEnvio(id, usuario.getId(), shippingCost);
        return ResponseEntity.ok(pedido);
    }

    /**
     * Preparar para pago - cambia estado a PENDING_PAYMENT
     * Se llama cuando el usuario está listo para pagar (después de completar facturación y envío)
     */
    @PostMapping("/checkout/{id}/prepare-payment")
    public ResponseEntity<PedidoResponse> prepararPago(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();
        AppUser usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        PedidoResponse pedido = pedidoService.prepararPago(id, usuario.getId());
        return ResponseEntity.ok(pedido);
    }
    
}
