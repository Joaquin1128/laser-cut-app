package com.example.lasercut.laser_cut_back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para actualizar datos de envío del pedido
 */
public class ShippingDataRequest {

    @NotNull(message = "El tipo de envío es obligatorio")
    private String shippingType; // "PICKUP" o "DELIVERY"

    // Campos requeridos solo si shippingType es "DELIVERY"
    private String street;

    private String unit; // Piso / Depto / Unidad (opcional)

    private String city;

    private String postalCode;

    private String province;

    private String country;

    public ShippingDataRequest() {
    }

    public String getShippingType() {
        return shippingType;
    }

    public void setShippingType(String shippingType) {
        this.shippingType = shippingType;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
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
}

