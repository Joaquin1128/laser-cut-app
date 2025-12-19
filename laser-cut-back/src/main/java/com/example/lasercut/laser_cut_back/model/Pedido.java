package com.example.lasercut.laser_cut_back.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * Entidad Pedido
 * 
 * PREPARACIÓN INTEGRACIÓN MERCADO PAGO:
 * - Se agregará campo: private String mercadoPagoPreferenceId;
 * - Se agregará campo: private String mercadoPagoPaymentId;
 * - Se agregará enum PaymentStatus: PENDING, APPROVED, REJECTED, CANCELLED
 * - Se agregará campo: private PaymentStatus paymentStatus;
 * 
 * Cuando se integre MP:
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

    @Column(nullable = false)
    private String material;

    @Column(nullable = false)
    private Double thickness;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(columnDefinition = "TEXT")
    private String metadata; // JSON string para información adicional del corte

    public Pedido() {
    }

    public Pedido(AppUser usuario, String material, Double thickness, Integer quantity, BigDecimal totalPrice) {
        this.usuario = usuario;
        this.material = material;
        this.thickness = thickness;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.status = OrderStatus.PENDIENTE;
        this.createdAt = LocalDateTime.now();
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

    public enum OrderStatus {
        PENDIENTE,
        EN_PROCESO,
        FINALIZADO,
        CANCELADO
    }
    
}
