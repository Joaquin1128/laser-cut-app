package com.example.lasercut.laser_cut_back.model;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

/**
 * Entidad Pedido
 * Representa UNA compra completa con múltiples items.
 * 
 * Un pedido contiene:
 * - Información del usuario
 * - Fecha de creación
 * - Estado del pedido
 * - Precio total (suma de todos los items)
 * - Lista de items (PedidoItem) - cada item es una pieza/producto
 * 
 * INTEGRACIÓN MERCADO PAGO:
 * - Al crear preferencia de pago, guardar preferenceId
 * - Al recibir webhook de MP, actualizar paymentStatus y paymentId
 * - Vincular paymentStatus con orderStatus
 */
@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser usuario;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice; // Suma de todos los items del pedido

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PedidoItem> items = new ArrayList<>();

    // Campos de Mercado Pago
    @Column(name = "mp_preference_id")
    private String mercadoPagoPreferenceId;

    @Column(name = "mp_payment_id")
    private String mercadoPagoPaymentId;

    @Column(name = "payment_status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    public Pedido() {
    }

    public Pedido(AppUser usuario, BigDecimal totalPrice) {
        this.usuario = usuario;
        this.totalPrice = totalPrice;
        this.status = OrderStatus.PENDIENTE;
        this.createdAt = LocalDateTime.now();
        this.items = new ArrayList<>();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AppUser getUsuario() {
        return usuario;
    }

    public void setUsuario(AppUser usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<PedidoItem> getItems() {
        return items;
    }

    public void setItems(List<PedidoItem> items) {
        this.items = items;
    }

    public void addItem(PedidoItem item) {
        this.items.add(item);
        item.setPedido(this);
    }

    public void removeItem(PedidoItem item) {
        this.items.remove(item);
        item.setPedido(null);
    }

    public String getMercadoPagoPreferenceId() {
        return mercadoPagoPreferenceId;
    }

    public void setMercadoPagoPreferenceId(String mercadoPagoPreferenceId) {
        this.mercadoPagoPreferenceId = mercadoPagoPreferenceId;
    }

    public String getMercadoPagoPaymentId() {
        return mercadoPagoPaymentId;
    }

    public void setMercadoPagoPaymentId(String mercadoPagoPaymentId) {
        this.mercadoPagoPaymentId = mercadoPagoPaymentId;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public enum OrderStatus {
        PENDIENTE,
        EN_PROCESO,
        FINALIZADO,
        CANCELADO
    }
    
    public enum PaymentStatus {
        PENDING,
        APPROVED,
        REJECTED,
        CANCELLED,
        REFUNDED
    }
    
}
