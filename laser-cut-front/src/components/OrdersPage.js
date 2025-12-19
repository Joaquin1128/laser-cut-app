import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ordersService } from '../services/ordersService';
import Header from './Header';
import AuthModal from './AuthModal';
import './OrdersPage.css';
import './QuotePage.css';
import './Wizard.css';
import './Step.css';

/**
 * Página de historial de pedidos
 * 
 * PREPARACIÓN INTEGRACIÓN MERCADO PAGO:
 * - Cuando se integre MP, aquí se mostrará el estado de pago de cada pedido
 * - Se agregará botón "Pagar" para pedidos pendientes de pago
 * - Se mostrará información del pago (approved, pending, rejected)
 */
function OrdersPage() {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const [pedidos, setPedidos] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showAuthModal, setShowAuthModal] = useState(false);

  useEffect(() => {
    if (!isAuthenticated) {
      setShowAuthModal(true);
      return;
    }
    cargarPedidos();
  }, [isAuthenticated]);

  const cargarPedidos = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const datos = await ordersService.obtenerPedidos();
      setPedidos(datos);
    } catch (err) {
      setError(err.message || 'Error al cargar los pedidos');
    } finally {
      setIsLoading(false);
    }
  };

  const formatearFecha = (fecha) => {
    if (!fecha) return '--';
    const date = new Date(fecha);
    return date.toLocaleDateString('es-AR', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const formatearEstado = (estado) => {
    const estados = {
      PENDIENTE: 'Pendiente',
      EN_PROCESO: 'En proceso',
      FINALIZADO: 'Finalizado',
      CANCELADO: 'Cancelado',
    };
    return estados[estado] || estado;
  };

  const formatearPrecio = (precio) => {
    if (!precio) return '$0';
    return new Intl.NumberFormat('es-AR', {
      style: 'currency',
      currency: 'ARS',
      minimumFractionDigits: 2,
    }).format(precio);
  };

  const handleContinueShopping = () => {
    navigate('/');
  };

  if (!isAuthenticated) {
    return (
      <div className="orders-page">
        <Header />
        {showAuthModal && (
          <AuthModal 
            onClose={() => {
              setShowAuthModal(false);
              navigate('/');
            }} 
          />
        )}
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="orders-page">
        <Header />
        <div className="orders-page-container">
          <div className="orders-loading">
            <div className="orders-spinner"></div>
            <p>Cargando pedidos...</p>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="orders-page">
        <Header />
        <div className="orders-page-container">
          <div className="orders-error">
            <p>{error}</p>
            <button
              type="button"
              className="btn-primary"
              onClick={cargarPedidos}
            >
              Reintentar
            </button>
          </div>
        </div>
      </div>
    );
  }

  if (pedidos.length === 0) {
    return (
      <div className="orders-page">
        <Header />
        <div className="orders-page-container">
          <div className="orders-empty">
            <h2 className="orders-empty-title">Todavía no realizaste pedidos</h2>
            <p className="orders-empty-message">Agregá piezas al carrito y procedé al pago para crear tu primer pedido.</p>
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
    <div className="orders-page">
      <Header />
      <div className="orders-page-container">
        <div className="orders-content wizard-step-material">
          <div className="step">
            <h3 className="step-title">Pedidos</h3>
            <p className="step-description">Revisá el historial de tus pedidos.</p>
          
            <div className="orders-list">
              {pedidos.map((pedido) => (
                <div key={pedido.id} className="order-item">
                  <div className="order-item-header">
                    <div className="order-item-id">
                      Pedido #{pedido.id}
                    </div>
                    <div className={`order-item-status order-status-${pedido.status.toLowerCase()}`}>
                      {formatearEstado(pedido.status)}
                    </div>
                  </div>
                  <div className="order-item-date">
                    {formatearFecha(pedido.createdAt)}
                  </div>
                  <div className="order-item-details">
                    <div className="order-item-info-row">
                      <span className="order-detail-label">Material:</span>
                      <span className="order-detail-value">{pedido.material}</span>
                    </div>
                    <div className="order-item-info-row">
                      <span className="order-detail-label">Espesor:</span>
                      <span className="order-detail-value">{pedido.thickness} mm</span>
                    </div>
                    <div className="order-item-info-row">
                      <span className="order-detail-label">Cantidad:</span>
                      <span className="order-detail-value">{pedido.quantity} piezas</span>
                    </div>
                  </div>
                  <div className="order-item-pricing">
                    <div className="order-item-price-row cart-item-total-row">
                      <span className="order-detail-label">Total:</span>
                      <span className="order-item-total">{formatearPrecio(pedido.totalPrice)}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            <div className="quote-actions">
              <button
                className="btn-secondary"
                type="button"
                onClick={handleContinueShopping}
              >
                COTIZAR MÁS PIEZAS
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default OrdersPage;
