package com.example.lasercut.laser_cut_back.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.lasercut.laser_cut_back.dto.CreatePedidoRequest;
import com.example.lasercut.laser_cut_back.dto.PedidoResponse;
import com.example.lasercut.laser_cut_back.dto.PreferenceResponse;
import com.example.lasercut.laser_cut_back.model.AppUser;
import com.example.lasercut.laser_cut_back.repository.UserRepository;
import com.example.lasercut.laser_cut_back.service.MercadoPagoService;
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
    private MercadoPagoService mercadoPagoService;

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
    
}
