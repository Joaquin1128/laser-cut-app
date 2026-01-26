import React from 'react';
import { useCart } from '../../../context/CartContext';
import '../CheckoutSteps.css';

function SummaryStep({
  pedido,
  billingData,
  shippingData,
  shippingCost,
  cartItems,
  onBack,
  onProceedToPayment,
  isProcessingPayment,
  paymentProgress,
}) {
  const { getCartTotal } = useCart();
  const subtotal = getCartTotal();
  const total = subtotal + shippingCost;

  return (
    <div className="checkout-step">
      <div className="step">
        <h3 className="step-title">Resumen del Pedido</h3>
        <p className="step-description">Revisá los datos antes de proceder al pago.</p>

        <div className="billing-shipping-layout">
          {/* Columna izquierda: Datos del pedido */}
          <div className="billing-shipping-form">
            {/* Resumen de Items */}
            <div className="summary-section">
              <h4 className="summary-section-title">Items del Pedido</h4>
              <div className="summary-items">
                {cartItems.map((item) => (
                  <div key={item.id} className="summary-item">
                    <div className="summary-item-info">
                      <span className="summary-item-name">{item.archivo?.nombre || 'Sin nombre'}</span>
                      <span className="summary-item-details">
                        {item.material?.nombre} - {item.material?.espesor}mm - {item.cantidad} unidades
                      </span>
                    </div>
                    <span className="summary-item-price">${item.precioTotal.toFixed(2)}</span>
                  </div>
                ))}
              </div>
            </div>

            {/* Datos de Facturación */}
            <div className="summary-section">
              <h4 className="summary-section-title">Datos de Facturación</h4>
              <div className="summary-data">
                <div className="summary-data-row">
                  <span>Nombre y Apellido:</span>
                  <span>{billingData.billingName}</span>
                </div>
                <div className="summary-data-row">
                  <span>DNI:</span>
                  <span>{billingData.fiscalId}</span>
                </div>
                <div className="summary-data-row">
                  <span>Email:</span>
                  <span>{billingData.billingEmail}</span>
                </div>
                <div className="summary-data-row">
                  <span>Teléfono:</span>
                  <span>{billingData.billingPhone}</span>
                </div>
              </div>
            </div>

            {/* Datos de Envío */}
            <div className="summary-section">
              <h4 className="summary-section-title">Envío</h4>
              <div className="summary-data">
                <div className="summary-data-row">
                  <span>Dirección:</span>
                  <span>
                    {shippingData.street}
                    {shippingData.unit && `, ${shippingData.unit}`}
                    {`, ${shippingData.city}, ${shippingData.postalCode}, ${shippingData.province}`}
                  </span>
                </div>
              </div>
            </div>
          </div>

          {/* Columna derecha: Resumen de Precios */}
          <div className="billing-shipping-sidebar">
            <div className="checkout-subtotal">
              <div className="subtotal-row">
                <span>Subtotal:</span>
                <span className="subtotal-amount">${subtotal.toFixed(2)}</span>
              </div>
              <div className="subtotal-row">
                <span>Envío:</span>
                <span className="subtotal-amount">${shippingCost.toFixed(2)}</span>
              </div>
              <div className="subtotal-row subtotal-total">
                <span>Total:</span>
                <span className="subtotal-amount">${total.toFixed(2)}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default SummaryStep;
