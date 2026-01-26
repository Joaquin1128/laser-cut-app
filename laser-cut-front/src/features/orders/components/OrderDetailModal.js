import React, { useEffect } from 'react';
import '../OrderDetailModal.css';

function OrderDetailModal({ pedido, onClose }) {
  useEffect(() => {
    const handleKeyDown = (event) => {
      if (event.key === 'Escape') {
        onClose?.();
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [onClose]);

  if (!pedido) {
    return null;
  }

  const handleOverlayClick = (event) => {
    if (event.target === event.currentTarget) {
      onClose?.();
    }
  };

  const formatearFecha = (fecha) => {
    if (!fecha) return '--';
    const date = new Date(fecha);
    return date.toLocaleDateString('es-AR', {
      year: 'numeric',
      month: 'long',
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

  const parseMetadata = (metadata) => {
    if (!metadata) return null;
    try {
      return JSON.parse(metadata);
    } catch {
      return null;
    }
  };

  return (
    <div
      className="order-detail-modal-overlay"
      role="dialog"
      aria-modal="true"
      onMouseDown={handleOverlayClick}
    >
      <div
        className="order-detail-modal"
        role="document"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <button
          className="order-detail-modal-close"
          type="button"
          onClick={() => onClose?.()}
          aria-label="Cerrar"
        >
          ×
        </button>

        <div className="order-detail-modal-header">
          <h2 className="order-detail-modal-title">Pedido #{pedido.id}</h2>
          <div className={`order-detail-status order-status-${pedido.status?.toLowerCase()}`}>
            {formatearEstado(pedido.status)}
          </div>
        </div>

        <div className="order-detail-modal-date">
          <span className="order-detail-label">Fecha:</span>
          <span className="order-detail-value">{formatearFecha(pedido.createdAt)}</span>
        </div>

        <div className="order-detail-modal-items">
          <h3 className="order-detail-section-title">Items del pedido ({pedido.items?.length || 0})</h3>
          {pedido.items && pedido.items.length > 0 ? (
            <div className="order-detail-items-list">
              {pedido.items.map((item) => {
                const metadata = parseMetadata(item.metadata);
                return (
                  <div key={item.id} className="order-detail-item">
                    <div className="order-detail-item-preview">
                      {metadata?.urlPreview && (
                        <img
                          src={metadata.urlPreview}
                          alt={metadata.archivoNombre || 'Pieza'}
                          className="order-detail-item-image"
                        />
                      )}
                    </div>
                    <div className="order-detail-item-info">
                      <h4 className="order-detail-item-name">
                        {metadata?.archivoNombre || 'Pieza sin nombre'}
                      </h4>
                      <div className="order-detail-item-details">
                        <div className="order-detail-item-row">
                          <span className="order-detail-label">Material:</span>
                          <span className="order-detail-value">{item.material}</span>
                        </div>
                        <div className="order-detail-item-row">
                          <span className="order-detail-label">Espesor:</span>
                          <span className="order-detail-value">{item.thickness} mm</span>
                        </div>
                        {metadata?.dimensiones && (
                          <div className="order-detail-item-row">
                            <span className="order-detail-label">Dimensiones:</span>
                            <span className="order-detail-value">{metadata.dimensiones}</span>
                          </div>
                        )}
                        {metadata?.terminacion && (
                          <div className="order-detail-item-row">
                            <span className="order-detail-label">Terminación:</span>
                            <span className="order-detail-value">{metadata.terminacion}</span>
                          </div>
                        )}
                        <div className="order-detail-item-row">
                          <span className="order-detail-label">Cantidad:</span>
                          <span className="order-detail-value">{item.quantity} unidades</span>
                        </div>
                        <div className="order-detail-item-row">
                          <span className="order-detail-label">Precio unitario:</span>
                          <span className="order-detail-value">{formatearPrecio(item.unitPrice)}</span>
                        </div>
                      </div>
                    </div>
                    <div className="order-detail-item-pricing">
                      <div className="order-detail-item-total">
                        <span className="order-detail-label">Subtotal:</span>
                        <span className="order-detail-price highlight">{formatearPrecio(item.totalPrice)}</span>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <p className="order-detail-empty">No hay items en este pedido.</p>
          )}
        </div>

        <div className="order-detail-modal-summary">
          <div className="order-detail-summary-row">
            <span className="order-detail-label">Total del pedido:</span>
            <span className="order-detail-total highlight">{formatearPrecio(pedido.totalPrice)}</span>
          </div>
          {pedido.paymentStatus && (
            <div className="order-detail-summary-row">
              <span className="order-detail-label">Estado del pago:</span>
              <span className={`order-detail-value payment-status-${pedido.paymentStatus?.toLowerCase()}`}>
                {pedido.paymentStatus}
              </span>
            </div>
          )}
        </div>

        <div className="order-detail-modal-actions">
          <button
            className="btn-primary"
            type="button"
            onClick={() => onClose?.()}
          >
            CERRAR
          </button>
        </div>
      </div>
    </div>
  );
}

export default OrderDetailModal;
