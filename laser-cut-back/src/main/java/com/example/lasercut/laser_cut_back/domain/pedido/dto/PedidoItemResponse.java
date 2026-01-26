package com.example.lasercut.laser_cut_back.domain.pedido.dto;

import java.math.BigDecimal;

import com.example.lasercut.laser_cut_back.domain.pedido.model.PedidoItem;

/**
 * DTO para respuesta de item de pedido
 */
public class PedidoItemResponse {
    
    private Long id;
    private String material;
    private Double thickness;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String metadata;

    public PedidoItemResponse() {
    }

    public PedidoItemResponse(PedidoItem item) {
        this.id = item.getId();
        this.material = item.getMaterial();
        this.thickness = item.getThickness();
        this.quantity = item.getQuantity();
        this.unitPrice = item.getUnitPrice();
        this.totalPrice = item.getTotalPrice();
        this.metadata = item.getMetadata();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
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

}
