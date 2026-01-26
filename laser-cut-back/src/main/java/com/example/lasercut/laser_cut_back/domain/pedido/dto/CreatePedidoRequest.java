package com.example.lasercut.laser_cut_back.domain.pedido.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO para crear un pedido
 * Un pedido contiene múltiples items (cada item es una pieza/producto del carrito)
 */
public class CreatePedidoRequest {

    @NotNull(message = "El precio total es obligatorio")
    @Positive(message = "El precio total debe ser positivo")
    private BigDecimal totalPrice;

    @NotEmpty(message = "El pedido debe tener al menos un item")
    @Valid
    private List<PedidoItemRequest> items = new ArrayList<>();

    public CreatePedidoRequest() {
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<PedidoItemRequest> getItems() {
        return items;
    }

    public void setItems(List<PedidoItemRequest> items) {
        this.items = items;
    }
    
}
