import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { ordersService } from '../../../services/ordersService';
import Header from '../../../shared/components/Header';
import OrderDetailModal from './OrderDetailModal';
import '../OrdersPage.css';
import '../../quote/QuotePage.css';
import '../../quote/Wizard.css';
import '../../quote/Step.css';

/**
 * Panel de administración - Lista todos los pedidos de todos los usuarios.
 * Solo accesible para usuarios con rol ADMIN.
 */
function AdminOrdersPage() {
  const navigate = useNavigate();
  const { isAuthenticated, user } = useAuth();
  const [pedidos, setPedidos] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedPedido, setSelectedPedido] = useState(null);
  const [showOrderDetail, setShowOrderDetail] = useState(false);

  const isAdmin = user?.role === 'ADMIN';

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/orders');
      return;
    }
    if (!isAdmin) {
      navigate('/orders');
      return;
    }
    cargarPedidos();
  }, [isAuthenticated, isAdmin, navigate]);

  const cargarPedidos = async () => {
    if (!isAdmin) return;
    setIsLoading(true);
    setError(null);
    try {
      const datos = await ordersService.obtenerTodosPedidosAdmin();
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

  const handleShowOrderDetail = (pedido) => {
    setSelectedPedido(pedido);
    setShowOrderDetail(true);
  };

  const handleCloseOrderDetail = () => {
    setShowOrderDetail(false);
    setSelectedPedido(null);
  };

  const handleBackToMyOrders = () => {
    navigate('/orders');
  };

  if (!isAuthenticated || !isAdmin) {
    return null; // Redirect handles this
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
            <button
              type="button"
              className="btn-secondary"
              onClick={handleBackToMyOrders}
              style={{ marginLeft: '0.5rem' }}
            >
              Volver a mis pedidos
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
            <div className="admin-orders-header">
              <h3 className="step-title">Panel Admin - Todos los pedidos</h3>
              <p className="step-description">
                Pedidos de todos los clientes con información de contacto.
              </p>
              <div className="admin-orders-actions">
                <button
                  className="btn-secondary"
                  type="button"
                  onClick={handleBackToMyOrders}
                >
                  ← Mis pedidos
                </button>
              </div>
            </div>

            {pedidos.length === 0 ? (
              <div className="orders-empty">
                <h2 className="orders-empty-title">No hay pedidos</h2>
                <p className="orders-empty-message">
                  Todavía no se registraron pedidos en el sistema.
                </p>
              </div>
            ) : (
              <div className="orders-list">
                {pedidos.map((pedido) => (
                  <div
                    key={pedido.id}
                    className="order-item admin-order-item"
                    onClick={() => handleShowOrderDetail(pedido)}
                    style={{ cursor: 'pointer' }}
                  >
                    <div className="order-item-header">
                      <div className="order-item-id">
                        Pedido #{pedido.id}
                      </div>
                      <div className={`order-item-status order-status-${(pedido.status || '').toLowerCase().replace(/_/g, '-')}`}>
                        {formatearEstado(pedido.status)}
                      </div>
                    </div>
                    <div className="order-item-date">
                      {formatearFecha(pedido.createdAt)}
                    </div>
                    {pedido.customerNombre && (
                      <div className="order-item-customer">
                        <span className="order-detail-label">Cliente:</span>
                        <span className="order-detail-value">
                          {pedido.customerNombre}
                          {pedido.customerEmail && ` (${pedido.customerEmail})`}
                        </span>
                      </div>
                    )}
                    <div className="order-item-details">
                      <div className="order-item-info-row">
                        <span className="order-detail-label">Items:</span>
                        <span className="order-detail-value">
                          {pedido.items?.length || 0}{' '}
                          {pedido.items?.length === 1 ? 'pieza' : 'piezas'}
                        </span>
                      </div>
                      <div className="order-item-info-row">
                        <span className="order-detail-label">Total:</span>
                        <span className="order-detail-value highlight">
                          {formatearPrecio(pedido.totalPrice)}
                        </span>
                      </div>
                    </div>
                    <div className="order-item-pricing">
                      <div className="order-item-price-row cart-item-total-row">
                        <span className="order-detail-label">Ver detalle →</span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {showOrderDetail && selectedPedido && (
        <OrderDetailModal
          pedido={selectedPedido}
          onClose={handleCloseOrderDetail}
          showCustomerInfo
        />
      )}
    </div>
  );
}

export default AdminOrdersPage;
