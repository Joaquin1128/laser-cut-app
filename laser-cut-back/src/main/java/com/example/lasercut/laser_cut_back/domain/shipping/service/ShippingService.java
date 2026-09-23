package com.example.lasercut.laser_cut_back.domain.shipping.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.lasercut.laser_cut_back.domain.shipping.dto.ShippingQuoteRequest;
import com.example.lasercut.laser_cut_back.domain.shipping.dto.ShippingQuoteResponse;
import com.example.lasercut.laser_cut_back.domain.shipping.provider.ShippingProvider;

/**
 * Servicio de envíos
 * Abstracción que permite usar diferentes proveedores de envío
 * 
 * Configuración: Usa el proveedor especificado en application.properties
 * - shipping.provider=mock (por defecto) o andreani
 */
@Service
public class ShippingService {

    @Autowired
    @Qualifier("customShippingProvider")
    private ShippingProvider customShippingProvider;

    public ShippingService(ShippingProvider customShippingProvider) {
        this.customShippingProvider = customShippingProvider;
    }

    public ShippingService() {
    }

    /**
     * Calcula el costo de envío para una dirección dada
     * 
     * @param request Datos de la dirección y peso
     * @return Cotización con costo y días estimados
     */
    public ShippingQuoteResponse calculateShipping(ShippingQuoteRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Shipping quote request is required");
        }

        // Si el peso no está especificado, usar un valor por defecto
        if (request.getTotalWeight() == null || request.getTotalWeight() <= 0) {
            request.setTotalWeight(1.0); // 1 kg por defecto
        }

        return customShippingProvider.calculateShipping(request);
    }

    /**
     * Obtiene el costo de retiro (siempre 0)
     * 
     * @return ShippingQuoteResponse con costo 0
     */
    public ShippingQuoteResponse getPickupCost() {
        ShippingQuoteResponse response = new ShippingQuoteResponse(
            BigDecimal.ZERO,
            "PICKUP",
            "0 días"
        );
        response.setMessage("Retiro en fábrica sin costo adicional.");
        return response;
    }

}
