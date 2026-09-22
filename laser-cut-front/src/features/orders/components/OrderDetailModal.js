import React, { useState, useEffect } from 'react';
import { ordersService } from '../../../services/ordersService';
import '../OrderDetailModal.css';

function OrderDetailModal({ pedido, onClose, showCustomerInfo = false, onStatusUpdated }) {
  const [currentPedido, setCurrentPedido] = useState(pedido);
  const [isUpdating, setIsUpdating] = useState(false);
  const [errorMsg, setErrorMsg] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  useEffect(() => {
    setCurrentPedido(pedido);
    setErrorMsg(null);
    setSuccessMsg(null);
  }, [pedido]);

  useEffect(() => {
    const handleKeyDown = (event) => {
      if (event.key === 'Escape') {
        onClose?.();
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [onClose]);

  if (!currentPedido) {
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
      PENDING_CHECKOUT: 'Checkout pendiente',
      PENDING_PAYMENT: 'Pago pendiente',
      PENDIENTE: 'Pendiente',
      EN_PROCESO: 'En proceso',
      PAID: 'Pagado',
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

  const handleCambiarEstado = async (nuevoEstado, extraData = {}) => {
    let confirmText = '';
    if (nuevoEstado === 'PAID') {
      confirmText = `¿Confirmar recepción del pago (Transferencia) para el Pedido #${currentPedido.id}? El estado pasará a "Pagado".`;
    } else if (nuevoEstado === 'EN_PROCESO') {
      confirmText = `¿Iniciar la fabricación para el Pedido #${currentPedido.id}? El estado pasará a "En proceso".`;
    } else if (nuevoEstado === 'FINALIZADO') {
      confirmText = `¿Marcar el Pedido #${currentPedido.id} como "Finalizado"?`;
    } else if (nuevoEstado === 'CANCELADO') {
      confirmText = `¿Estás seguro de cancelar el Pedido #${currentPedido.id}? Esta acción es definitiva.`;
    }

    if (confirmText && !window.confirm(confirmText)) {
      return;
    }

    setIsUpdating(true);
    setErrorMsg(null);
    setSuccessMsg(null);

    try {
      const updated = await ordersService.actualizarEstadoPedidoAdmin(currentPedido.id, {
        nuevoEstado,
        ...extraData,
      });
      setCurrentPedido(updated);
      setSuccessMsg(`Estado actualizado a "${formatearEstado(updated.status)}" correctamente.`);
      onStatusUpdated?.(updated);
    } catch (err) {
      setErrorMsg(err.message || 'Error al actualizar el estado del pedido');
    } finally {
      setIsUpdating(false);
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
          <h2 className="order-detail-modal-title">Pedido #{currentPedido.id}</h2>
          <div className={`order-detail-status order-status-${(currentPedido.status || '').toLowerCase().replace(/_/g, '-')}`}>
            {formatearEstado(currentPedido.status)}
          </div>
        </div>

        <div className="order-detail-modal-date">
          <span className="order-detail-label">Fecha:</span>
          <span className="order-detail-value">{formatearFecha(currentPedido.createdAt)}</span>
        </div>

        {(showCustomerInfo && (currentPedido.customerNombre || currentPedido.customerEmail)) && (
          <div className="order-detail-modal-customer">
            <h3 className="order-detail-section-title">Cliente</h3>
            <div className="order-detail-item-row">
              <span className="order-detail-label">Nombre:</span>
              <span className="order-detail-value">{currentPedido.customerNombre || '--'}</span>
            </div>
            <div className="order-detail-item-row">
              <span className="order-detail-label">Email:</span>
              <span className="order-detail-value">{currentPedido.customerEmail || '--'}</span>
            </div>
          </div>
        )}

        <div className="order-detail-modal-items">
          <h3 className="order-detail-section-title">Items del pedido ({currentPedido.items?.length || 0})</h3>
          {currentPedido.items && currentPedido.items.length > 0 ? (
            <div className="order-detail-items-list">
              {currentPedido.items.map((item) => {
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
            <span className="order-detail-total highlight">{formatearPrecio(currentPedido.totalPrice)}</span>
          </div>
          {currentPedido.paymentStatus && (
            <div className="order-detail-summary-row">
              <span className="order-detail-label">Estado del pago:</span>
              <span className={`order-detail-value payment-status-${currentPedido.paymentStatus?.toLowerCase()}`}>
                {currentPedido.paymentStatus}
              </span>
            </div>
          )}
          {currentPedido.paymentMethod && (
            <div className="order-detail-summary-row">
              <span className="order-detail-label">Método de pago:</span>
              <span className="order-detail-value">
                {currentPedido.paymentMethod}
              </span>
            </div>
          )}
        </div>

        {showCustomerInfo && (
          <div className="order-detail-admin-panel">
            <h3 className="order-detail-section-title">Gestión del Pedido (Admin)</h3>

            {errorMsg && (
              <div className="order-admin-feedback error">
                {errorMsg}
              </div>
            )}
            {successMsg && (
              <div className="order-admin-feedback success">
                {successMsg}
              </div>
            )}

            <div className="order-admin-actions">
              {currentPedido.status === 'PENDING_CHECKOUT' && (
                <>
                  <p className="order-admin-hint">El cliente aún no ha completado el checkout.</p>
                  <button
                    type="button"
                    className="btn-admin-cancel"
                    disabled={isUpdating}
                    onClick={() => handleCambiarEstado('CANCELADO')}
                  >
                    {isUpdating ? 'Actualizando...' : '✕ Cancelar Pedido'}
                  </button>
                </>
              )}

              {currentPedido.status === 'PENDING_PAYMENT' && (
                <>
                  <button
                    type="button"
                    className="btn-admin-confirm"
                    disabled={isUpdating}
                    onClick={() => handleCambiarEstado('PAID', { paymentMethod: 'TRANSFERENCIA' })}
                  >
                    {isUpdating ? 'Actualizando...' : '✓ Confirmar Pago (Transferencia)'}
                  </button>
                  <button
                    type="button"
                    className="btn-admin-cancel"
                    disabled={isUpdating}
                    onClick={() => handleCambiarEstado('CANCELADO')}
                  >
                    {isUpdating ? 'Actualizando...' : '✕ Cancelar Pedido'}
                  </button>
                </>
              )}

              {currentPedido.status === 'PAID' && (
                <>
                  <button
                    type="button"
                    className="btn-admin-process"
                    disabled={isUpdating}
                    onClick={() => handleCambiarEstado('EN_PROCESO')}
                  >
                    {isUpdating ? 'Actualizando...' : '⚙ Iniciar Fabricación (En Proceso)'}
                  </button>
                  <button
                    type="button"
                    className="btn-admin-cancel"
                    disabled={isUpdating}
                    onClick={() => handleCambiarEstado('CANCELADO')}
                  >
                    {isUpdating ? 'Actualizando...' : '✕ Cancelar Pedido'}
                  </button>
                </>
              )}

              {currentPedido.status === 'EN_PROCESO' && (
                <>
                  <button
                    type="button"
                    className="btn-admin-finish"
                    disabled={isUpdating}
                    onClick={() => handleCambiarEstado('FINALIZADO')}
                  >
                    {isUpdating ? 'Actualizando...' : '✓ Marcar como Finalizado'}
                  </button>
                  <button
                    type="button"
                    className="btn-admin-cancel"
                    disabled={isUpdating}
                    onClick={() => handleCambiarEstado('CANCELADO')}
                  >
                    {isUpdating ? 'Actualizando...' : '✕ Cancelar Pedido'}
                  </button>
                </>
              )}

              {currentPedido.status === 'FINALIZADO' && (
                <div className="order-admin-terminal-badge finalized">
                  ✓ Este pedido ya se encuentra finalizado. No requiere más acciones.
                </div>
              )}

              {currentPedido.status === 'CANCELADO' && (
                <div className="order-admin-terminal-badge cancelled">
                  ✕ Este pedido fue cancelado. No se pueden realizar más cambios.
                </div>
              )}
            </div>
          </div>
        )}

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
