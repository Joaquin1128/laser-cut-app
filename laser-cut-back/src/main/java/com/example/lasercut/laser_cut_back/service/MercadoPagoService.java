package com.example.lasercut.laser_cut_back.service;

import com.example.lasercut.laser_cut_back.dto.PreferenceResponse;
import com.example.lasercut.laser_cut_back.exception.BadRequestException;
import com.example.lasercut.laser_cut_back.model.Pedido;
import com.google.gson.Gson;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de integración con Mercado Pago
 * 
 * IMPORTANTE:
 * - Configurar ACCESS_TOKEN en application.properties
 * - En producción, usar credenciales de producción
 * - Configurar webhook URL en el dashboard de Mercado Pago
 */
@Service
public class MercadoPagoService {

    @Value("${mercado.pago.access.token}")
    private String accessToken;

    @Value("${mercado.pago.webhook.url}")
    private String webhookUrl;

    public MercadoPagoService(@Value("${mercado.pago.access.token}") String accessToken) {
        MercadoPagoConfig.setAccessToken(accessToken);
    }

    /**
     * Crea una preferencia de pago en Mercado Pago para un pedido
     * 
     * @param pedido El pedido para el cual crear la preferencia
     * @param backUrls URLs de retorno después del pago
     * @return La preferencia creada con init_point para redirigir al usuario
     */
    public PreferenceResponse crearPreferencia(Pedido pedido, String successUrl, String failureUrl, String pendingUrl) {
        try {
            PreferenceClient client = new PreferenceClient();

            // Validar que el precio sea válido
            if (pedido.getTotalPrice() == null || pedido.getTotalPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("El precio total del pedido debe ser mayor a cero");
            }

            if (pedido.getQuantity() == null || pedido.getQuantity() <= 0) {
                throw new BadRequestException("La cantidad debe ser mayor a cero");
            }

            // Calcular precio unitario
            BigDecimal unitPrice = pedido.getTotalPrice().divide(
                BigDecimal.valueOf(pedido.getQuantity()), 
                2, 
                java.math.RoundingMode.HALF_UP
            );

            // Validar que el precio unitario sea válido para MP (mínimo 0.01)
            if (unitPrice.compareTo(new BigDecimal("0.01")) < 0) {
                throw new BadRequestException("El precio unitario debe ser al menos $0.01");
            }

            // Limitar longitud de título y descripción (MP tiene límites)
            String title = "Pedido #" + pedido.getId() + " - " + pedido.getMaterial();
            if (title.length() > 127) {
                title = title.substring(0, 124) + "...";
            }
            
            String description = "Corte láser: " + pedido.getMaterial() + " (" + pedido.getThickness() + "mm) - " + pedido.getQuantity() + " piezas";
            if (description.length() > 127) {
                description = description.substring(0, 124) + "...";
            }

            // Crear items de la preferencia
            List<PreferenceItemRequest> items = new ArrayList<>();
            
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title(title)
                    .description(description)
                    .quantity(pedido.getQuantity())
                    .unitPrice(unitPrice)
                    .currencyId("ARS")
                    .build();
            
            System.out.println("Creando preferencia MP - Pedido ID: " + pedido.getId() + 
                             ", UnitPrice: " + unitPrice + 
                             ", Quantity: " + pedido.getQuantity() + 
                             ", TotalPrice: " + pedido.getTotalPrice());
            
            items.add(item);

            // Validar que las URLs no sean nulas o vacías
            if (successUrl == null || successUrl.trim().isEmpty()) {
                throw new BadRequestException("La URL de éxito no puede estar vacía");
            }
            if (failureUrl == null || failureUrl.trim().isEmpty()) {
                throw new BadRequestException("La URL de fallo no puede estar vacía");
            }
            if (pendingUrl == null || pendingUrl.trim().isEmpty()) {
                throw new BadRequestException("La URL de pendiente no puede estar vacía");
            }

            System.out.println("URLs recibidas - Success: " + successUrl + 
                             ", Failure: " + failureUrl + 
                             ", Pending: " + pendingUrl);

            // Crear backUrls según documentación oficial de Mercado Pago
            // https://www.mercadopago.com.ar/developers/es/docs/checkout-pro/configure-back-urls
            com.mercadopago.client.preference.PreferenceBackUrlsRequest backUrls = 
                com.mercadopago.client.preference.PreferenceBackUrlsRequest.builder()
                    .success(successUrl.trim())
                    .failure(failureUrl.trim())
                    .pending(pendingUrl.trim())
                    .build();

            // Crear la preferencia (sin autoReturn según documentación oficial)
            // La redirección automática está habilitada por defecto cuando se configuran las backUrls
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .externalReference(String.valueOf(pedido.getId()))
                    .notificationUrl(webhookUrl)
                    .backUrls(backUrls)
                    .statementDescriptor("G2 LaserLab") // Descripción que aparece en el resumen de tarjeta
                    .build();
            
            System.out.println("PreferenceRequest construido correctamente con backUrls");

            Preference preference = client.create(preferenceRequest);

            // Crear respuesta DTO
            PreferenceResponse response = new PreferenceResponse();
            response.setId(preference.getId());
            response.setInitPoint(preference.getInitPoint());
            response.setSandboxInitPoint(preference.getSandboxInitPoint());
            if (preference.getClientId() != null) {
                response.setClientId(preference.getClientId());
            }

            return response;
        } catch (com.mercadopago.exceptions.MPApiException e) {
            // Error de la API de Mercado Pago
            String errorMessage = "Error de Mercado Pago: ";
            if (e.getApiResponse() != null) {
                if (e.getApiResponse().getContent() != null) {
                    errorMessage += e.getApiResponse().getContent();
                }
                if (e.getApiResponse().getHeaders() != null) {
                    System.err.println("MP Response Headers: " + e.getApiResponse().getHeaders());
                }
            }
            if (errorMessage.equals("Error de Mercado Pago: ")) {
                errorMessage += e.getMessage();
            }
            System.err.println("MP API Error Status: " + e.getStatusCode());
            System.err.println("MP API Error Message: " + errorMessage);
            if (e.getCause() != null) {
                System.err.println("MP API Error Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            throw new BadRequestException(errorMessage);
        } catch (com.mercadopago.exceptions.MPException e) {
            // Error general de Mercado Pago
            System.err.println("MP Exception: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("MP Exception Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            throw new BadRequestException("Error al crear preferencia de pago: " + e.getMessage());
        } catch (Exception e) {
            // Otros errores
            System.err.println("Error general: " + e.getMessage());
            e.printStackTrace();
            throw new BadRequestException("Error al crear preferencia de pago: " + e.getMessage());
        }
    }

    /**
     * Procesa un webhook de Mercado Pago
     * 
     * @param data Datos del webhook
     * @return El ID del pago procesado
     */
    public String procesarWebhook(String data) {
        try {
            // Parsear el webhook (formato: {"type":"payment","data":{"id":"123456789"}})
            // En producción, validar la firma del webhook usando x-signature header
            Gson gson = new Gson();
            WebhookData webhookData = gson.fromJson(data, WebhookData.class);
            
            if ("payment".equals(webhookData.getType()) && webhookData.getData() != null) {
                return webhookData.getData().getId();
            }
            
            return null;
        } catch (Exception e) {
            throw new BadRequestException("Error al procesar webhook: " + e.getMessage());
        }
    }

    /**
     * Obtiene el estado de un pago desde Mercado Pago
     * 
     * @param paymentId ID del pago
     * @return Estado del pago (approved, rejected, pending, etc.)
     */
    public String obtenerEstadoPago(String paymentId) {
        try {
            com.mercadopago.client.payment.PaymentClient client = new com.mercadopago.client.payment.PaymentClient();
            com.mercadopago.resources.payment.Payment payment = client.get(Long.parseLong(paymentId));
            return payment.getStatus();
        } catch (Exception e) {
            throw new BadRequestException("Error al obtener estado del pago: " + e.getMessage());
        }
    }

    // Clases auxiliares para parsear webhook
    private static class WebhookData {
        private String type;
        private WebhookPaymentData data;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public WebhookPaymentData getData() {
            return data;
        }

        public void setData(WebhookPaymentData data) {
            this.data = data;
        }
    }

    private static class WebhookPaymentData {
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

}
