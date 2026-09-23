import React, { useState } from 'react';
import { useCart } from '../../../context/CartContext';
import { ordersService } from '../../../services/ordersService';
import '../CheckoutSteps.css';

function BillingShippingStep({
  billingData,
  setBillingData,
  shippingData,
  setShippingData,
  shippingCost,
  setShippingCost,
  shippingQuote,
  setShippingQuote,
  pedidoId,
  onNext,
  onBack,
  isLoading,
}) {
  const { cartItems, getCartTotal } = useCart();
  const [isCalculatingShipping, setIsCalculatingShipping] = useState(false);
  const [quoteError, setQuoteError] = useState(null);
  const subtotal = getCartTotal();

  const handleBillingChange = (field, value) => {
    setBillingData(prev => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleShippingChange = (field, value) => {
    setShippingData(prev => ({
      ...prev,
      [field]: value,
    }));
  };

  const calculateTotalWeight = () => {
    if (!cartItems || cartItems.length === 0) return 1.0;
    let totalKg = 0;
    cartItems.forEach(item => {
      const ancho = item.archivo?.dimensiones?.ancho || item.archivo?.ancho || 100;
      const alto = item.archivo?.dimensiones?.alto || item.archivo?.alto || 100;
      const espesor = item.material?.espesorSeleccionado || item.material?.espesor?.espesorMm || item.espesor || 1.5;
      const densidad = item.material?.densidad || 7.85; // g/cm³ estándar para metales
      const cantidad = item.cantidad || 1;
      const pesoUnitario = (ancho * alto * espesor * densidad) / 1000000.0;
      totalKg += pesoUnitario * cantidad;
    });
    return Math.max(0.5, Math.round(totalKg * 100) / 100);
  };

  const calculateShipping = async () => {
    if (!shippingData.street || !shippingData.city || !shippingData.postalCode || !shippingData.province) {
      setQuoteError('Por favor completá todos los campos de dirección');
      return;
    }

    setIsCalculatingShipping(true);
    setQuoteError(null);

    try {
      const totalWeight = calculateTotalWeight();

      const quoteRequest = {
        street: shippingData.street,
        city: shippingData.city,
        postalCode: shippingData.postalCode,
        province: shippingData.province,
        country: 'Argentina',
        totalWeight: totalWeight,
        destinatarioNombre: billingData.billingName,
        destinatarioTelefono: billingData.billingPhone,
        destinatarioEmail: billingData.billingEmail,
      };

      const quote = await ordersService.calcularEnvio(quoteRequest);
      setShippingQuote(quote);
      setShippingCost(parseFloat(quote.shippingCost));
    } catch (err) {
      console.error('Error al calcular envío:', err);
      setQuoteError(err.message || 'Error al calcular el costo de envío');
    } finally {
      setIsCalculatingShipping(false);
    }
  };

  return (
    <div className="checkout-step">
      <div className="step">
        <h3 className="step-title">Facturación y envío</h3>
        <p className="step-description">Completá los datos para la facturación y el envío.</p>

        <div className="billing-shipping-layout">
          {/* Columna izquierda: Formulario */}
          <div className="billing-shipping-form">
            <div className="form-section">
              <input
                type="text"
                className="auth-form-input"
                value={billingData.billingName}
                onChange={(e) => handleBillingChange('billingName', e.target.value)}
                placeholder="Nombre y Apellido *"
                required
              />
            </div>

            <div className="form-section">
              <input
                type="text"
                className="auth-form-input"
                value={billingData.fiscalId}
                onChange={(e) => handleBillingChange('fiscalId', e.target.value)}
                placeholder="DNI *"
                required
              />
            </div>

            <div className="form-section">
              <input
                type="email"
                className="auth-form-input"
                value={billingData.billingEmail}
                onChange={(e) => handleBillingChange('billingEmail', e.target.value)}
                placeholder="Email *"
                required
              />
            </div>

            <div className="form-section">
              <input
                type="tel"
                className="auth-form-input"
                value={billingData.billingPhone}
                onChange={(e) => handleBillingChange('billingPhone', e.target.value)}
                placeholder="Teléfono *"
                required
              />
            </div>

            <div className="form-section">
              <input
                type="text"
                className="auth-form-input"
                value={shippingData.street}
                onChange={(e) => handleShippingChange('street', e.target.value)}
                placeholder="Calle y Número *"
                required
              />
            </div>

            <div className="form-section">
              <input
                type="text"
                className="auth-form-input"
                value={shippingData.unit}
                onChange={(e) => handleShippingChange('unit', e.target.value)}
                placeholder="Piso / Depto / Unidad"
              />
            </div>

            <div className="form-section">
              <input
                type="text"
                className="auth-form-input"
                value={shippingData.city}
                onChange={(e) => handleShippingChange('city', e.target.value)}
                placeholder="Ciudad / Localidad *"
                required
              />
            </div>

            <div className="form-row">
              <div className="form-section">
                <input
                  type="text"
                  className="auth-form-input"
                  value={shippingData.postalCode}
                  onChange={(e) => handleShippingChange('postalCode', e.target.value)}
                  placeholder="Código Postal *"
                  required
                />
              </div>

              <div className="form-section">
                <input
                  type="text"
                  className="auth-form-input"
                  value={shippingData.province}
                  onChange={(e) => handleShippingChange('province', e.target.value)}
                  placeholder="Provincia *"
                  required
                />
              </div>
            </div>
          </div>

          {/* Columna derecha: Panel de cálculo y subtotal */}
          <div className="billing-shipping-sidebar">
            {/* Campo con botón para calcular envío */}
            <div className="shipping-calc-section">
              <div className="shipping-calc-input-group">
                <input
                  type="text"
                  className="auth-form-input"
                  placeholder="C.P."
                  value={shippingData.postalCode || ''}
                  onChange={(e) => handleShippingChange('postalCode', e.target.value)}
                />
                <button
                  type="button"
                  className="btn-primary"
                  onClick={calculateShipping}
                  disabled={isCalculatingShipping || !shippingData.street || !shippingData.city || !shippingData.postalCode || !shippingData.province}
                >
                  {isCalculatingShipping ? 'CALCULANDO...' : 'CALCULAR COSTO ENVÍO'}
                </button>
              </div>
              {quoteError && (
                <p className="error-message" style={{ marginTop: '0.5rem', color: '#dc3545', fontSize: '0.875rem' }}>
                  {quoteError}
                </p>
              )}
              {shippingQuote && (
                <div className="shipping-quote-result">
                  <div className="shipping-quote-row">
                    <span>Costo:</span>
                    <span className="shipping-cost">${parseFloat(shippingQuote.shippingCost).toFixed(2)}</span>
                  </div>
                  {shippingQuote.estimatedDays && (
                    <div className="shipping-quote-row">
                      <span>Tiempo:</span>
                      <span>{shippingQuote.estimatedDays}</span>
                    </div>
                  )}
                  {shippingQuote.message && (
                    <p style={{ marginTop: '0.35rem', fontSize: '0.8rem', color: '#666', fontStyle: 'italic' }}>
                      {shippingQuote.message}
                    </p>
                  )}
                </div>
              )}
            </div>

            {/* Subtotal */}
            <div className="checkout-subtotal">
              <div className="subtotal-row">
                <span>Subtotal:</span>
                <span className="subtotal-amount">${subtotal.toFixed(2)}</span>
              </div>
              <div className="subtotal-row">
                <span>Envío:</span>
                <span className="subtotal-amount">${shippingCost > 0 ? shippingCost.toFixed(2) : '0.00'}</span>
              </div>
              <div className="subtotal-row subtotal-total">
                <span>Total:</span>
                <span className="subtotal-amount">${(subtotal + shippingCost).toFixed(2)}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default BillingShippingStep;
