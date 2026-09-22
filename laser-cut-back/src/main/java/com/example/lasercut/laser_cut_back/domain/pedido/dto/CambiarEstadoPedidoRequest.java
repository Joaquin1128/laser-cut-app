package com.example.lasercut.laser_cut_back.domain.pedido.dto;

import jakarta.validation.constraints.NotBlank;

public class CambiarEstadoPedidoRequest {

    @NotBlank(message = "El nuevo estado es obligatorio")
    private String nuevoEstado;

    private String paymentMethod;

    private String motivo;

    public CambiarEstadoPedidoRequest() {
    }

    public CambiarEstadoPedidoRequest(String nuevoEstado, String paymentMethod, String motivo) {
        this.nuevoEstado = nuevoEstado;
        this.paymentMethod = paymentMethod;
        this.motivo = motivo;
    }

    public String getNuevoEstado() {
        return nuevoEstado;
    }

    public void setNuevoEstado(String nuevoEstado) {
        this.nuevoEstado = nuevoEstado;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
    
}
