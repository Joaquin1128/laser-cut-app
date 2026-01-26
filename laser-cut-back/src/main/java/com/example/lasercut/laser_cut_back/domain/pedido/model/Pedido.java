package com.example.lasercut.laser_cut_back.domain.pedido.model;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import com.example.lasercut.laser_cut_back.domain.auth.model.AppUser;

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

    // Campos de facturación
    @Column(name = "billing_name")
    private String billingName; // Nombre o razón social

    @Column(name = "billing_email")
    private String billingEmail;

    @Column(name = "billing_type")
    private String billingType; // "A" (Factura A), "B" (Factura B), "C" (Factura C), "CONSUMIDOR_FINAL"

    @Column(name = "fiscal_id")
    private String fiscalId; // DNI (para Factura C)

    @Column(name = "billing_phone")
    private String billingPhone; // Teléfono de contacto

    // Campos de envío
    @Column(name = "shipping_type")
    @Enumerated(EnumType.STRING)
    private ShippingType shippingType; // PICKUP o DELIVERY

    @Column(name = "shipping_cost", precision = 10, scale = 2)
    private BigDecimal shippingCost = BigDecimal.ZERO;

    @Column(name = "shipping_address_street")
    private String shippingAddressStreet;

    @Column(name = "shipping_address_unit")
    private String shippingAddressUnit; // Piso / Depto / Unidad

    @Column(name = "shipping_address_city")
    private String shippingAddressCity;

    @Column(name = "shipping_address_postal_code")
    private String shippingAddressPostalCode;

    @Column(name = "shipping_address_province")
    private String shippingAddressProvince;

    @Column(name = "shipping_address_country")
    private String shippingAddressCountry;

    public Pedido() {
    }

    public Pedido(AppUser usuario, BigDecimal totalPrice) {
        this.usuario = usuario;
        this.totalPrice = totalPrice;
        this.status = OrderStatus.PENDING_CHECKOUT; // Estado inicial: en proceso de checkout
        this.createdAt = LocalDateTime.now();
        this.items = new ArrayList<>();
        this.shippingCost = BigDecimal.ZERO;
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
        PENDING_CHECKOUT,  // Pedido creado, en proceso de checkout
        PENDING_PAYMENT,   // Checkout completo, esperando pago
        PENDIENTE,         // Deprecado: usar PENDING_PAYMENT
        EN_PROCESO,
        PAID,              // Pago aprobado (equivalente a EN_PROCESO después de pago)
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

    public enum ShippingType {
        PICKUP,    // Retiro en fábrica
        DELIVERY   // Envío a domicilio
    }

    // Getters y Setters para facturación
    public String getBillingName() {
        return billingName;
    }

    public void setBillingName(String billingName) {
        this.billingName = billingName;
    }

    public String getBillingEmail() {
        return billingEmail;
    }

    public void setBillingEmail(String billingEmail) {
        this.billingEmail = billingEmail;
    }

    public String getBillingType() {
        return billingType;
    }

    public void setBillingType(String billingType) {
        this.billingType = billingType;
    }

    public String getFiscalId() {
        return fiscalId;
    }

    public void setFiscalId(String fiscalId) {
        this.fiscalId = fiscalId;
    }

    public String getBillingPhone() {
        return billingPhone;
    }

    public void setBillingPhone(String billingPhone) {
        this.billingPhone = billingPhone;
    }

    // Getters y Setters para envío
    public ShippingType getShippingType() {
        return shippingType;
    }

    public void setShippingType(ShippingType shippingType) {
        this.shippingType = shippingType;
    }

    public BigDecimal getShippingCost() {
        return shippingCost != null ? shippingCost : BigDecimal.ZERO;
    }

    public void setShippingCost(BigDecimal shippingCost) {
        this.shippingCost = shippingCost != null ? shippingCost : BigDecimal.ZERO;
    }

    public String getShippingAddressStreet() {
        return shippingAddressStreet;
    }

    public void setShippingAddressStreet(String shippingAddressStreet) {
        this.shippingAddressStreet = shippingAddressStreet;
    }

    public String getShippingAddressUnit() {
        return shippingAddressUnit;
    }

    public void setShippingAddressUnit(String shippingAddressUnit) {
        this.shippingAddressUnit = shippingAddressUnit;
    }

    public String getShippingAddressCity() {
        return shippingAddressCity;
    }

    public void setShippingAddressCity(String shippingAddressCity) {
        this.shippingAddressCity = shippingAddressCity;
    }

    public String getShippingAddressPostalCode() {
        return shippingAddressPostalCode;
    }

    public void setShippingAddressPostalCode(String shippingAddressPostalCode) {
        this.shippingAddressPostalCode = shippingAddressPostalCode;
    }

    public String getShippingAddressProvince() {
        return shippingAddressProvince;
    }

    public void setShippingAddressProvince(String shippingAddressProvince) {
        this.shippingAddressProvince = shippingAddressProvince;
    }

    public String getShippingAddressCountry() {
        return shippingAddressCountry;
    }

    public void setShippingAddressCountry(String shippingAddressCountry) {
        this.shippingAddressCountry = shippingAddressCountry;
    }

    /**
     * Obtiene el precio total incluyendo envío
     */
    public BigDecimal getTotalWithShipping() {
        return totalPrice.add(getShippingCost());
    }
    
}
