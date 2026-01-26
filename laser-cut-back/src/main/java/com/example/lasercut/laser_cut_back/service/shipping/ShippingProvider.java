package com.example.lasercut.laser_cut_back.service.shipping;

import java.math.BigDecimal;

import com.example.lasercut.laser_cut_back.dto.ShippingQuoteRequest;
import com.example.lasercut.laser_cut_back.dto.ShippingQuoteResponse;

/**
 * Interfaz para proveedores de envío
 * Permite integrar diferentes servicios de envío (Andreani, OCA, etc.)
 */
public interface ShippingProvider {

    /**
     * Calcula el costo de envío para una dirección dada
     * 
     * @param request Datos de la dirección y peso
     * @return Cotización con costo y días estimados
     */
    ShippingQuoteResponse calculateShipping(ShippingQuoteRequest request);

    /**
     * Obtiene el nombre del proveedor
     * 
     * @return Nombre del proveedor (ej: "ANDREANI", "MOCK")
     */
    String getProviderName();
}

