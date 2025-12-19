import React, { useState, useEffect } from 'react';
import { ordersService } from '../services/ordersService';
import './OrdersModal.css';

/**
 * Modal de historial de pedidos
 * 
 * PREPARACIÓN INTEGRACIÓN MERCADO PAGO:
 * - Cuando se integre MP, aquí se mostrará el estado de pago de cada pedido
 * - Se agregará botón "Pagar" para pedidos pendientes de pago
 * - Se mostrará información del pago (approved, pending, rejected)
 */
function OrdersModal({ onClose }) {
  const [pedidos, setPedidos] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    cargarPedidos();
  }, []);

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

  useEffect(() => {
    const handleKeyDown = (event) => {
      if (event.key === 'Escape') {
        onClose();
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [onClose]);

  const handleOverlayClick = (event) => {
    if (event.target === event.currentTarget) {
      onClose();
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

  return (
    <div
      className="orders-modal-overlay"
      role="dialog"
      aria-modal="true"
      onMouseDown={handleOverlayClick}
    >
      <div
        className="orders-modal"
        role="document"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <button
          className="orders-modal-close"
          type="button"
          onClick={onClose}
          aria-label="Cerrar"
        >
          ×
        </button>

        <div className="orders-modal-content">
          <h2 className="orders-modal-title">Mis Pedidos</h2>

          {isLoading ? (
            <div className="orders-loading">
              <div className="orders-spinner"></div>
              <p>Cargando pedidos...</p>
            </div>
          ) : error ? (
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
          ) : pedidos.length === 0 ? (
            <div className="orders-empty">
              <p className="orders-empty-text">
                Todavía no realizaste pedidos
              </p>
            </div>
          ) : (
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
                    <div className="order-detail">
                      <span className="order-detail-label">Material:</span>
                      <span className="order-detail-value">{pedido.material}</span>
                    </div>
                    <div className="order-detail">
                      <span className="order-detail-label">Espesor:</span>
                      <span className="order-detail-value">{pedido.thickness} mm</span>
                    </div>
                    <div className="order-detail">
                      <span className="order-detail-label">Cantidad:</span>
                      <span className="order-detail-value">{pedido.quantity} piezas</span>
                    </div>
                  </div>
                  <div className="order-item-footer">
                    <div className="order-item-total">
                      Total: {formatearPrecio(pedido.totalPrice)}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default OrdersModal;
