package com.example.lasercut.laser_cut_back.domain.pedido.dto;

import com.example.lasercut.laser_cut_back.domain.pedido.model.Pedido;

/**
 * DTO de pedido con datos del cliente (usuario).
 * Usado por el admin para listar todos los pedidos con información del cliente.
 */
public class PedidoWithCustomerResponse extends PedidoResponse {

    private Long customerId;
    private String customerNombre;
    private String customerEmail;

    public PedidoWithCustomerResponse() {
    }

    public PedidoWithCustomerResponse(Pedido pedido) {
        super(pedido);
        if (pedido.getUsuario() != null) {
            this.customerId = pedido.getUsuario().getId();
            this.customerNombre = pedido.getUsuario().getNombre();
            this.customerEmail = pedido.getUsuario().getEmail();
        }
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerNombre() {
        return customerNombre;
    }

    public void setCustomerNombre(String customerNombre) {
        this.customerNombre = customerNombre;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
}
