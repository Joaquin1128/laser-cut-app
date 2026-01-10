package com.example.lasercut.laser_cut_back.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.lasercut.laser_cut_back.model.Pedido;

/**
 * DTO para respuesta de pedido
 */
public class PedidoResponse {
    private Long id;
    private LocalDateTime createdAt;
    private String status;
    private String material;
    private Double thickness;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String metadata;
    private String paymentStatus; // Estado del pago de Mercado Pago

    public PedidoResponse() {
    }

    public PedidoResponse(Pedido pedido) {
        this.id = pedido.getId();
        this.createdAt = pedido.getCreatedAt();
        this.status = pedido.getStatus().name();
        this.material = pedido.getMaterial();
        this.thickness = pedido.getThickness();
        this.quantity = pedido.getQuantity();
        this.totalPrice = pedido.getTotalPrice();
        this.metadata = pedido.getMetadata();
        if (pedido.getPaymentStatus() != null) {
            this.paymentStatus = pedido.getPaymentStatus().name();
        }
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

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public Double getThickness() {
        return thickness;
    }

    public void setThickness(Double thickness) {
        this.thickness = thickness;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
    
    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
}
