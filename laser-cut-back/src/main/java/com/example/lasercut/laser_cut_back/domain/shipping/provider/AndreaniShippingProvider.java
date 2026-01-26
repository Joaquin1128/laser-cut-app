package com.example.lasercut.laser_cut_back.domain.shipping.provider;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.lasercut.laser_cut_back.domain.shipping.dto.ShippingQuoteRequest;
import com.example.lasercut.laser_cut_back.domain.shipping.dto.ShippingQuoteResponse;
import com.example.lasercut.laser_cut_back.domain.shipping.provider.AndreaniAuthService;

/**
 * Proveedor de envío Andreani
 * Integración con la API de Andreani para calcular costos de envío
 * 
 * NOTA: La documentación proporcionada es para crear órdenes de envío.
 * Para cotización, puede ser necesario usar un endpoint diferente o crear una orden de prueba.
 * Esta implementación está preparada para ambos casos.
 */
@Component("andreaniShippingProvider")
public class AndreaniShippingProvider implements ShippingProvider {

    private final RestTemplate restTemplate;
    private final AndreaniAuthService authService;
    
    @Value("${andreani.api.url:https://apis.andreani.com}")
    private String apiUrl;
    
    @Value("${andreani.api.url.qa:https://apisqa.andreani.com}")
    private String apiUrlQa;
    
    @Value("${andreani.api.environment:prod}")
    private String environment; // prod o qa
    
    @Value("${andreani.contract.number:}")
    private String contractNumber;
    
    @Value("${andreani.client.number:}")
    private String clientNumber;
    
    @Value("${andreani.origin.postalCode:C1000}")
    private String originPostalCode;
    
    @Value("${andreani.origin.sucursal:}")
    private String sucursalOrigen;
    
    @Value("${andreani.origin.street:}")
    private String originStreet;
    
    @Value("${andreani.origin.number:}")
    private String originNumber;
    
    @Value("${andreani.origin.city:}")
    private String originCity;
    
    @Value("${andreani.origin.province:}")
    private String originProvince;
    
    @Value("${andreani.remitente.nombre:}")
    private String remitenteNombre;
    
    @Value("${andreani.remitente.email:}")
    private String remitenteEmail;
    
    @Value("${andreani.remitente.telefono:}")
    private String remitenteTelefono;

    public AndreaniShippingProvider(AndreaniAuthService authService) {
        this.restTemplate = new RestTemplate();
        this.authService = authService;
    }
    
    private String getBaseUrl() {
        return "qa".equalsIgnoreCase(environment) ? apiUrlQa : apiUrl;
    }

    @Override
    public ShippingQuoteResponse calculateShipping(ShippingQuoteRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Shipping quote request is required");
        }

        // Validar que tenemos el contrato y cliente configurados
        if (contractNumber == null || contractNumber.isEmpty()) {
            throw new IllegalStateException("El número de contrato de Andreani no está configurado");
        }
        if (clientNumber == null || clientNumber.isEmpty()) {
            throw new IllegalStateException("El código de cliente de Andreani no está configurado");
        }

        try {
            // Obtener token de autenticación
            String accessToken = authService.getAccessToken();
            
            // Construir URL con query parameters para el endpoint de cotización
            String quoteEndpoint = buildQuoteUrl(request);
            
            HttpHeaders headers = createHeaders(accessToken);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.exchange(
                quoteEndpoint,
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            // Parsear la respuesta de cotización
            return parseQuoteResponse(response.getBody());
            
        } catch (RestClientException e) {
            throw new RuntimeException("Error al calcular envío con Andreani: " + e.getMessage(), e);
        }
    }
    
    /**
     * Construye la URL del endpoint de cotización con query parameters
     * Endpoint: GET /v1/tarifas
     */
    private String buildQuoteUrl(ShippingQuoteRequest request) {
        StringBuilder url = new StringBuilder(getBaseUrl() + "/v1/tarifas?");
        
        // Parámetros obligatorios
        url.append("cpDestino=").append(request.getPostalCode());
        url.append("&contrato=").append(contractNumber);
        url.append("&cliente=").append(clientNumber);
        
        // Sucursal origen (opcional)
        if (sucursalOrigen != null && !sucursalOrigen.isEmpty()) {
            url.append("&sucursalOrigen=").append(sucursalOrigen);
        }
        
        // Calcular volumen en cm³
        double volumenCm3 = calculateVolumeCm3(request.getTotalWeight());
        
        // Bultos - parámetros obligatorios y opcionales
        url.append("&bultos[0][volumen]=").append(volumenCm3);
        url.append("&bultos[0][kilos]=").append(request.getTotalWeight());
        
        // Dimensiones opcionales (estimar si no están disponibles)
        url.append("&bultos[0][largoCm]=30");
        url.append("&bultos[0][altoCm]=20");
        url.append("&bultos[0][anchoCm]=15");
        
        // Valor declarado (opcional) - podría obtenerse del pedido
        // url.append("&bultos[0][valorDeclarado]=").append(valorDeclarado);
        
        return url.toString();
    }
    
    /**
     * Construye el request para crear una orden de envío según la documentación
     * NOTA: Este método se usa para crear órdenes reales, no para cotizar
     * Para cotizar, usar el endpoint GET /v1/tarifas
     */
    @SuppressWarnings("unused")
    private Map<String, Object> buildOrderRequest(ShippingQuoteRequest request) {
        Map<String, Object> orderRequest = new HashMap<>();
        
        // Contrato (obligatorio)
        orderRequest.put("contrato", contractNumber);
        
        // Origen (obligatorio)
        Map<String, Object> origen = new HashMap<>();
        Map<String, Object> origenPostal = new HashMap<>();
        origenPostal.put("codigoPostal", originPostalCode);
        origenPostal.put("calle", originStreet);
        origenPostal.put("numero", originNumber);
        origenPostal.put("localidad", originCity);
        origenPostal.put("pais", "Argentina");
        if (originProvince != null && !originProvince.isEmpty()) {
            origenPostal.put("region", originProvince);
        }
        origen.put("postal", origenPostal);
        orderRequest.put("origen", origen);
        
        // Destino (obligatorio)
        Map<String, Object> destino = new HashMap<>();
        Map<String, Object> destinoPostal = new HashMap<>();
        destinoPostal.put("codigoPostal", request.getPostalCode());
        destinoPostal.put("calle", request.getStreet());
        // Extraer número de la calle si está incluido
        String[] streetParts = request.getStreet().split("\\s+");
        if (streetParts.length > 0) {
            destinoPostal.put("numero", streetParts[streetParts.length - 1]);
        } else {
            destinoPostal.put("numero", "");
        }
        destinoPostal.put("localidad", request.getCity());
        destinoPostal.put("pais", request.getCountry() != null ? request.getCountry() : "Argentina");
        if (request.getProvince() != null && !request.getProvince().isEmpty()) {
            destinoPostal.put("region", request.getProvince());
        }
        // Nota: piso y departamento no están disponibles en ShippingQuoteRequest
        // Si se necesitan, deberían agregarse al DTO
        destino.put("postal", destinoPostal);
        orderRequest.put("destino", destino);
        
        // Remitente (obligatorio)
        Map<String, Object> remitente = new HashMap<>();
        remitente.put("nombreCompleto", remitenteNombre);
        if (remitenteEmail != null && !remitenteEmail.isEmpty()) {
            remitente.put("email", remitenteEmail);
        }
        if (remitenteTelefono != null && !remitenteTelefono.isEmpty()) {
            List<Map<String, Object>> telefonos = new ArrayList<>();
            Map<String, Object> telefono = new HashMap<>();
            telefono.put("tipo", 1); // Celular = 1
            telefono.put("numero", remitenteTelefono);
            telefonos.add(telefono);
            remitente.put("telefonos", telefonos);
        }
        orderRequest.put("remitente", remitente);
        
        // Destinatario (obligatorio)
        List<Map<String, Object>> destinatarios = new ArrayList<>();
        Map<String, Object> destinatario = new HashMap<>();
        
        // Usar datos del request si están disponibles, sino usar valores por defecto
        String nombreDestinatario = request.getDestinatarioNombre() != null && !request.getDestinatarioNombre().isEmpty()
            ? request.getDestinatarioNombre()
            : "Destinatario"; // Valor por defecto si no está disponible
        
        String telefonoDestinatario = request.getDestinatarioTelefono() != null && !request.getDestinatarioTelefono().isEmpty()
            ? request.getDestinatarioTelefono()
            : "1111111111"; // Valor por defecto si no está disponible
        
        destinatario.put("nombreCompleto", nombreDestinatario);
        
        if (request.getDestinatarioEmail() != null && !request.getDestinatarioEmail().isEmpty()) {
            destinatario.put("email", request.getDestinatarioEmail());
        }
        
        List<Map<String, Object>> telefonosDest = new ArrayList<>();
        Map<String, Object> telefonoDest = new HashMap<>();
        telefonoDest.put("tipo", 1); // Celular = 1
        telefonoDest.put("numero", telefonoDestinatario);
        telefonosDest.add(telefonoDest);
        destinatario.put("telefonos", telefonosDest);
        destinatarios.add(destinatario);
        orderRequest.put("destinatario", destinatarios);
        
        // Bultos (obligatorio)
        List<Map<String, Object>> bultos = new ArrayList<>();
        Map<String, Object> bulto = new HashMap<>();
        bulto.put("kilos", request.getTotalWeight());
        // Calcular volumen aproximado (cm³)
        double volumenCm3 = calculateVolumeCm3(request.getTotalWeight());
        bulto.put("volumenCm", volumenCm3);
        // Dimensiones opcionales (estimar si no están disponibles)
        bulto.put("largoCm", 30.0); // TODO: Obtener dimensiones reales
        bulto.put("altoCm", 20.0);
        bulto.put("anchoCm", 15.0);
        bultos.add(bulto);
        orderRequest.put("bultos", bultos);
        
        return orderRequest;
    }
    
    /**
     * Parsea la respuesta del endpoint de cotización
     * Response format:
     * {
     *   "pesoAforado": "70.00",
     *   "tarifaSinIva": {
     *     "seguroDistribucion": "12.21",
     *     "distribucion": "5806.97",
     *     "total": "5819.18"
     *   },
     *   "tarifaConIva": {
     *     "seguroDistribucion": "14.77",
     *     "distribucion": "7026.43",
     *     "total": "7041.21"
     *   }
     * }
     */
    @SuppressWarnings("unchecked")
    private ShippingQuoteResponse parseQuoteResponse(Map<String, Object> responseBody) {
        if (responseBody == null) {
            throw new RuntimeException("Respuesta vacía de Andreani");
        }
        
        // Extraer tarifa con IVA (total)
        Map<String, Object> tarifaConIva = (Map<String, Object>) responseBody.get("tarifaConIva");
        if (tarifaConIva == null) {
            throw new RuntimeException("La respuesta de Andreani no contiene tarifaConIva");
        }
        
        Object totalObj = tarifaConIva.get("total");
        BigDecimal shippingCost;
        try {
            if (totalObj instanceof Number) {
                shippingCost = BigDecimal.valueOf(((Number) totalObj).doubleValue());
            } else if (totalObj instanceof String) {
                shippingCost = new BigDecimal((String) totalObj);
            } else {
                throw new RuntimeException("Formato de costo inválido en la respuesta de Andreani: " + totalObj);
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("No se pudo parsear el costo de envío de Andreani: " + totalObj, e);
        }
        
        // Estimar días de entrega (no viene en la respuesta de cotización)
        String estimatedDays = estimateDaysFromPostalCode(responseBody);
        
        ShippingQuoteResponse quote = new ShippingQuoteResponse(
            shippingCost,
            "ANDREANI",
            estimatedDays
        );
        
        // Agregar información adicional si está disponible
        Object pesoAforado = responseBody.get("pesoAforado");
        if (pesoAforado != null) {
            quote.setMessage("Cotización calculada. Peso aforado: " + pesoAforado + " kg");
        } else {
            quote.setMessage("Cotización calculada por Andreani");
        }
        
        return quote;
    }
    
    /**
     * Estima días de entrega basado en el código postal
     * (La API de cotización no devuelve esta información)
     */
    private String estimateDaysFromPostalCode(Map<String, Object> responseBody) {
        // Por defecto, estimar días basado en distancia
        // En producción, esto podría venir de otra consulta o configuración
        return "5-7 días";
    }
    
    /**
     * Calcula volumen en cm³ basado en el peso
     */
    private double calculateVolumeCm3(double weightKg) {
        // Estimación: densidad promedio de productos de corte láser
        // Asumir densidad de ~0.5 g/cm³ (productos metálicos/láminas)
        return weightKg * 2000; // cm³ aproximado
    }

    /**
     * Crea los headers HTTP necesarios para la autenticación con Andreani
     * Según la documentación: "debes enviar el access token por header"
     */
    private HttpHeaders createHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken); // Bearer token según documentación
        
        return headers;
    }

    @Override
    public String getProviderName() {
        return "ANDREANI";
    }
}
