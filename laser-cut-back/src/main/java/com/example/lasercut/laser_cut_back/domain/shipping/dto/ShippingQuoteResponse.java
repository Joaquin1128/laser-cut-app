package com.example.lasercut.laser_cut_back.domain.shipping.dto;

import java.math.BigDecimal;

/**
 * DTO para respuesta de cotización de envío
 */
public class ShippingQuoteResponse {

    private BigDecimal shippingCost;
    private String provider; // "ANDREANI", "MOCK", etc.
    private String estimatedDays;
    private String message; // Mensaje informativo (ej: "Cotización estimada")

    public ShippingQuoteResponse() {
    }

    public ShippingQuoteResponse(BigDecimal shippingCost, String provider, String estimatedDays) {
        this.shippingCost = shippingCost;
        this.provider = provider;
        this.estimatedDays = estimatedDays;
    }

    public BigDecimal getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(BigDecimal shippingCost) {
        this.shippingCost = shippingCost;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getEstimatedDays() {
        return estimatedDays;
    }

    public void setEstimatedDays(String estimatedDays) {
        this.estimatedDays = estimatedDays;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
