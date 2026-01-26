package com.example.lasercut.laser_cut_back.domain.shipping.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para solicitar cotización de envío
 */
public class ShippingQuoteRequest {

    @NotBlank(message = "La calle es obligatoria")
    private String street;

    @NotBlank(message = "La ciudad es obligatoria")
    private String city;

    @NotBlank(message = "El código postal es obligatorio")
    private String postalCode;

    @NotBlank(message = "La provincia es obligatoria")
    private String province;

    private String country = "Argentina"; // Por defecto

    @NotNull(message = "El peso total es obligatorio")
    private Double totalWeight; // Peso total en kg
    
    // Campos opcionales para datos del destinatario (necesarios para Andreani)
    private String destinatarioNombre; // Nombre del destinatario
    private String destinatarioTelefono; // Teléfono del destinatario
    private String destinatarioEmail; // Email del destinatario

    public ShippingQuoteRequest() {
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Double getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(Double totalWeight) {
        this.totalWeight = totalWeight;
    }

    public String getDestinatarioNombre() {
        return destinatarioNombre;
    }

    public void setDestinatarioNombre(String destinatarioNombre) {
        this.destinatarioNombre = destinatarioNombre;
    }

    public String getDestinatarioTelefono() {
        return destinatarioTelefono;
    }

    public void setDestinatarioTelefono(String destinatarioTelefono) {
        this.destinatarioTelefono = destinatarioTelefono;
    }

    public String getDestinatarioEmail() {
        return destinatarioEmail;
    }

    public void setDestinatarioEmail(String destinatarioEmail) {
        this.destinatarioEmail = destinatarioEmail;
    }
}
