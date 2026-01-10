import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import { ordersService } from '../services/ordersService';
import Header from './Header';
import { FaTrash } from 'react-icons/fa';
import AuthModal from './AuthModal';
import './CartPage.css';
import './QuotePage.css';
import './Wizard.css';
import './Step.css';

function CartPage() {
  const navigate = useNavigate();
  const { cartItems, removeFromCart, clearCart, getCartTotal } = useCart();
  const { isAuthenticated } = useAuth();
  const [showAuthModal, setShowAuthModal] = useState(false);
  const [isCreatingOrder, setIsCreatingOrder] = useState(false);
  const total = getCartTotal();

  const handleContinueShopping = () => {
    navigate('/upload');
  };

  const handleProceedToPayment = async () => {
    // Si no está autenticado, mostrar modal de login
    if (!isAuthenticated) {
      setShowAuthModal(true);
      return;
    }

    setIsCreatingOrder(true);
    try {
      // 1. Crear pedidos primero (uno por cada item del carrito)
      const pedidosCreados = await Promise.all(
        cartItems.map(async (item) => {
          const pedidoData = {
            material: item.material?.nombre || 'Desconocido',
            thickness: item.material?.espesor || 0,
            quantity: item.cantidad || 1,
            totalPrice: item.precioTotal || 0,
            metadata: JSON.stringify({
              archivoNombre: item.archivo?.nombre,
              dimensiones: item.archivo?.dimensiones,
              terminacion: item.terminacion,
            }),
          };
          return ordersService.crearPedido(pedidoData);
        })
      );

      // 2. Consolidar todos los pedidos en una sola preferencia
      // Crear una preferencia con todos los items del carrito
      if (pedidosCreados.length > 0) {
        // Usar el primer pedido como referencia, pero crear preferencia con el total
        const totalPedidos = pedidosCreados.reduce((sum, p) => sum + parseFloat(p.totalPrice), 0);
        const primerPedido = pedidosCreados[0];
        
        // URLs de retorno
        const baseUrl = window.location.origin;
        const urls = {
          successUrl: `${baseUrl}/payment/success?status=approved`,
          failureUrl: `${baseUrl}/payment/failure?status=rejected`,
          pendingUrl: `${baseUrl}/payment/pending?status=pending`,
        };

        // Crear preferencia de pago solo para el primer pedido
        // TODO: En el futuro, crear una preferencia consolidada con todos los items
        const preference = await ordersService.crearPreferenciaPago(primerPedido.id, urls);
        
        // Limpiar carrito antes de redirigir
        clearCart();
        
        // Redirigir al checkout de Mercado Pago
        // Usar sandboxInitPoint en desarrollo, initPoint en producción
        const checkoutUrl = preference.sandboxInitPoint || preference.initPoint;
        if (checkoutUrl) {
          window.location.href = checkoutUrl;
        } else {
          throw new Error('No se pudo obtener la URL de checkout');
        }
      }
      
    } catch (error) {
      console.error('Error al procesar el pago:', error);
      alert('Error al procesar el pago. Por favor, intentá nuevamente.');
      setIsCreatingOrder(false);
    }
  };

  if (cartItems.length === 0) {
    return (
      <div className="cart-page">
        <Header />
        <div className="cart-page-container">
          <div className="cart-empty">
            <h2 className="cart-empty-title">Tu carrito está vacío</h2>
            <p className="cart-empty-message">Agregá piezas desde la página de cotización.</p>
            <button
              className="btn-primary"
              type="button"
              onClick={handleContinueShopping}
            >
              COTIZAR PIEZAS
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="cart-page">
      <Header />
      <div className="cart-page-container">
        <div className="cart-content wizard-step-material">
          <div className="step">
            <h3 className="step-title">Carrito</h3>
            <p className="step-description">Revisá los items agregados a tu carrito.</p>
          
          <div className="cart-items">
            {cartItems.map((item) => (
              <div key={item.id} className="cart-item">
                <div className="cart-item-preview">
                  {item.archivo?.urlPreview && (
                    <img
                      src={item.archivo.urlPreview}
                      alt={item.archivo.nombre || 'Pieza'}
                      className="cart-item-image"
                    />
                  )}
                </div>
                
                <div className="cart-item-details">
                  <h3 className="cart-item-name">{item.archivo?.nombre || 'Sin nombre'}</h3>
                  <div className="cart-item-info">
                    <div className="cart-item-info-row">
                      <span className="cart-item-label">Material:</span>
                      <span className="cart-item-value">{item.material?.nombre || '--'}</span>
                    </div>
                    <div className="cart-item-info-row">
                      <span className="cart-item-label">Espesor:</span>
                      <span className="cart-item-value">{item.material?.espesor || '--'} mm</span>
                    </div>
                    {item.terminacion && (
                      <div className="cart-item-info-row">
                        <span className="cart-item-label">Terminación:</span>
                        <span className="cart-item-value">{item.terminacion}</span>
                      </div>
                    )}
                    {item.archivo?.dimensiones && (
                      <div className="cart-item-info-row">
                        <span className="cart-item-label">Dimensiones:</span>
                        <span className="cart-item-value">{item.archivo.dimensiones}</span>
                      </div>
                    )}
                    <div className="cart-item-info-row">
                      <span className="cart-item-label">Cantidad:</span>
                      <span className="cart-item-value">{item.cantidad} unidades</span>
                    </div>
                  </div>
                </div>

                <div className="cart-item-pricing">
                  <div className="cart-item-price-row">
                    <span className="cart-item-label">Precio unitario:</span>
                    <span className="cart-item-price">${item.precioUnitario.toFixed(2)}</span>
                  </div>
                  <div className="cart-item-price-row cart-item-total-row">
                    <span className="cart-item-label">Total:</span>
                    <span className="cart-item-price highlight">${item.precioTotal.toFixed(2)}</span>
                  </div>
                </div>

                <button
                  className="cart-item-remove"
                  type="button"
                  onClick={() => removeFromCart(item.id)}
                  aria-label="Eliminar item"
                >
                  <FaTrash />
                </button>
              </div>
            ))}
          </div>

          <div className="cart-summary">
            <div className="cart-summary-row">
              <span className="cart-summary-label">Total general:</span>
              <span className="cart-summary-total">${total.toFixed(2)}</span>
            </div>
          </div>

          <div className="quote-actions">
            <button
              className="btn-secondary"
              type="button"
              onClick={handleContinueShopping}
            >
              SEGUIR COTIZANDO
            </button>
            <button
              className="btn-primary"
              type="button"
              onClick={handleProceedToPayment}
              disabled={isCreatingOrder}
            >
              {isCreatingOrder ? 'PROCESANDO...' : 'PROCEDER AL PAGO'}
            </button>
          </div>
          </div>
        </div>
      </div>

      {showAuthModal && (
        <AuthModal onClose={() => setShowAuthModal(false)} />
      )}
    </div>
  );
}

export default CartPage;
