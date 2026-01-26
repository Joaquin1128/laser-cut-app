package com.example.lasercut.laser_cut_back.domain.pedido.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO para crear un item de pedido
 */
public class PedidoItemRequest {

    @NotBlank(message = "El material es obligatorio")
    private String material;

    @NotNull(message = "El espesor es obligatorio")
    @Positive(message = "El espesor debe ser positivo")
    private Double thickness;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer quantity;

    @NotNull(message = "El precio unitario es obligatorio")
    @Positive(message = "El precio unitario debe ser positivo")
    private BigDecimal unitPrice;

    @NotNull(message = "El precio total es obligatorio")
    @Positive(message = "El precio total debe ser positivo")
    private BigDecimal totalPrice;

    private String metadata; // Opcional: JSON con información adicional (nombre archivo, dimensiones, terminación, etc.)

    public PedidoItemRequest() {
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
