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
    
}
