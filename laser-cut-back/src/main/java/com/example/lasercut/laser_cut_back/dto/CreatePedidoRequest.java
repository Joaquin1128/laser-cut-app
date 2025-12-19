package com.example.lasercut.laser_cut_back.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO para crear un pedido
 */
public class CreatePedidoRequest {

    @NotBlank(message = "El material es obligatorio")
    private String material;

    @NotNull(message = "El espesor es obligatorio")
    @Positive(message = "El espesor debe ser positivo")
    private Double thickness;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer quantity;

    @NotNull(message = "El precio total es obligatorio")
    @Positive(message = "El precio total debe ser positivo")
    private BigDecimal totalPrice;

    private String metadata; // Opcional

    public CreatePedidoRequest() {
    }

    public CreatePedidoRequest(String material, Double thickness, Integer quantity, BigDecimal totalPrice) {
        this.material = material;
        this.thickness = thickness;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
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

}
