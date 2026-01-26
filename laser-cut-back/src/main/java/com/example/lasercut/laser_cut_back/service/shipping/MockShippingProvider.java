package com.example.lasercut.laser_cut_back.service.shipping;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.example.lasercut.laser_cut_back.dto.ShippingQuoteRequest;
import com.example.lasercut.laser_cut_back.dto.ShippingQuoteResponse;

/**
 * Proveedor mock de envío para desarrollo y testing
 * Calcula un costo estimado basado en el peso y código postal
 * 
 * FUTURA IMPLEMENTACIÓN: Reemplazar con integración real de Andreani u otro proveedor
 */
@Component("mockShippingProvider")
public class MockShippingProvider implements ShippingProvider {

    private static final BigDecimal BASE_COST = new BigDecimal("1500.00");
    private static final BigDecimal COST_PER_KG = new BigDecimal("500.00");
    private static final BigDecimal MAX_COST = new BigDecimal("5000.00");

    @Override
    public ShippingQuoteResponse calculateShipping(ShippingQuoteRequest request) {
        if (request == null || request.getTotalWeight() == null) {
            throw new IllegalArgumentException("Total weight is required");
        }

        // Cálculo simplificado: costo base + (peso * costo por kg)
        double weight = request.getTotalWeight();
        BigDecimal calculatedCost = BASE_COST.add(
            COST_PER_KG.multiply(BigDecimal.valueOf(weight))
        );

        // Limitar costo máximo
        BigDecimal finalCost = calculatedCost.compareTo(MAX_COST) > 0 
            ? MAX_COST 
            : calculatedCost;

        // Días estimados basados en el código postal (simplificado)
        String estimatedDays = calculateEstimatedDays(request.getPostalCode());

        ShippingQuoteResponse response = new ShippingQuoteResponse(
            finalCost,
            getProviderName(),
            estimatedDays
        );
        response.setMessage("Cotización estimada. El costo final puede variar según el destino.");

        return response;
    }

    /**
     * Calcula días estimados basado en código postal (simplificado)
     * En producción, esto vendría de la API del proveedor
     */
    private String calculateEstimatedDays(String postalCode) {
        if (postalCode == null || postalCode.isEmpty()) {
            return "5-7 días";
        }

        // Simplificación: códigos postales de CABA suelen ser más rápidos
        if (postalCode.startsWith("C1") || postalCode.length() == 4) {
            return "3-5 días";
        }

        // Gran Buenos Aires
        if (postalCode.startsWith("B")) {
            return "5-7 días";
        }

        // Otras provincias
        return "7-10 días";
    }

    @Override
    public String getProviderName() {
        return "MOCK";
    }
}

