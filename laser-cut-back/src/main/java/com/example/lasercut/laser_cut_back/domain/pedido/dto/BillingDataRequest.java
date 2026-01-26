package com.example.lasercut.laser_cut_back.domain.pedido.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para actualizar datos de facturación del pedido
 */
public class BillingDataRequest {

    @NotBlank(message = "El nombre o razón social es obligatorio")
    private String billingName;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String billingEmail;

    @NotBlank(message = "El DNI es obligatorio")
    private String fiscalId; // DNI para Factura C

    @NotBlank(message = "El teléfono es obligatorio")
    private String billingPhone; // Teléfono de contacto

    // billingType siempre será tipo C
    private String billingType; // Siempre será "C"

    public BillingDataRequest() {
    }

    public String getBillingName() {
        return billingName;
    }

    public void setBillingName(String billingName) {
        this.billingName = billingName;
    }

    public String getBillingEmail() {
        return billingEmail;
    }

    public void setBillingEmail(String billingEmail) {
        this.billingEmail = billingEmail;
    }

    public String getBillingType() {
        return billingType;
    }

    public void setBillingType(String billingType) {
        this.billingType = billingType;
    }

    public String getFiscalId() {
        return fiscalId;
    }

    public void setFiscalId(String fiscalId) {
        this.fiscalId = fiscalId;
    }

    public String getBillingPhone() {
        return billingPhone;
    }

    public void setBillingPhone(String billingPhone) {
        this.billingPhone = billingPhone;
    }
}
