package com.example.lasercut.laser_cut_back.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.example.lasercut.laser_cut_back.model.Pedido;

/**
 * DTO para respuesta de pedido
 * Incluye la lista de items del pedido
 */
public class PedidoResponse {
    
    private Long id;
    private LocalDateTime createdAt;
    private String status;
    private BigDecimal totalPrice;
    private List<PedidoItemResponse> items;
    private String paymentStatus; // Estado del pago de Mercado Pago

    // Datos de facturación
    private String billingName;
    private String billingEmail;
    private String billingType;
    private String fiscalId;
    private String billingPhone;

    // Datos de envío
    private String shippingType;
    private BigDecimal shippingCost;
    private String shippingAddressStreet;
    private String shippingAddressUnit;
    private String shippingAddressCity;
    private String shippingAddressPostalCode;
    private String shippingAddressProvince;
    private String shippingAddressCountry;

    // Total con envío
    private BigDecimal totalWithShipping;

    public PedidoResponse() {
    }

    public PedidoResponse(Pedido pedido) {
        this.id = pedido.getId();
        this.createdAt = pedido.getCreatedAt();
        this.status = pedido.getStatus().name();
        this.totalPrice = pedido.getTotalPrice();
        this.items = pedido.getItems().stream()
                .map(PedidoItemResponse::new)
                .collect(Collectors.toList());
        if (pedido.getPaymentStatus() != null) {
            this.paymentStatus = pedido.getPaymentStatus().name();
        }

        // Datos de facturación
        this.billingName = pedido.getBillingName();
        this.billingEmail = pedido.getBillingEmail();
        this.billingType = pedido.getBillingType();
        this.fiscalId = pedido.getFiscalId();
        this.billingPhone = pedido.getBillingPhone();

        // Datos de envío
        if (pedido.getShippingType() != null) {
            this.shippingType = pedido.getShippingType().name();
        }
        this.shippingCost = pedido.getShippingCost();
        this.shippingAddressStreet = pedido.getShippingAddressStreet();
        this.shippingAddressUnit = pedido.getShippingAddressUnit();
        this.shippingAddressCity = pedido.getShippingAddressCity();
        this.shippingAddressPostalCode = pedido.getShippingAddressPostalCode();
        this.shippingAddressProvince = pedido.getShippingAddressProvince();
        this.shippingAddressCountry = pedido.getShippingAddressCountry();

        // Total con envío
        this.totalWithShipping = pedido.getTotalWithShipping();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<PedidoItemResponse> getItems() {
        return items;
    }

    public void setItems(List<PedidoItemResponse> items) {
        this.items = items;
    }
    
    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    // Getters y Setters para facturación
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

    // Getters y Setters para envío
    public String getShippingType() {
        return shippingType;
    }

    public void setShippingType(String shippingType) {
        this.shippingType = shippingType;
    }

    public BigDecimal getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(BigDecimal shippingCost) {
        this.shippingCost = shippingCost;
    }

    public String getShippingAddressStreet() {
        return shippingAddressStreet;
    }

    public void setShippingAddressStreet(String shippingAddressStreet) {
        this.shippingAddressStreet = shippingAddressStreet;
    }

    public String getShippingAddressUnit() {
        return shippingAddressUnit;
    }

    public void setShippingAddressUnit(String shippingAddressUnit) {
        this.shippingAddressUnit = shippingAddressUnit;
    }

    public String getShippingAddressCity() {
        return shippingAddressCity;
    }

    public void setShippingAddressCity(String shippingAddressCity) {
        this.shippingAddressCity = shippingAddressCity;
    }

    public String getShippingAddressPostalCode() {
        return shippingAddressPostalCode;
    }

    public void setShippingAddressPostalCode(String shippingAddressPostalCode) {
        this.shippingAddressPostalCode = shippingAddressPostalCode;
    }

    public String getShippingAddressProvince() {
        return shippingAddressProvince;
    }

    public void setShippingAddressProvince(String shippingAddressProvince) {
        this.shippingAddressProvince = shippingAddressProvince;
    }

    public String getShippingAddressCountry() {
        return shippingAddressCountry;
    }

    public void setShippingAddressCountry(String shippingAddressCountry) {
        this.shippingAddressCountry = shippingAddressCountry;
    }

    public BigDecimal getTotalWithShipping() {
        return totalWithShipping;
    }

    public void setTotalWithShipping(BigDecimal totalWithShipping) {
        this.totalWithShipping = totalWithShipping;
    }
    
}
