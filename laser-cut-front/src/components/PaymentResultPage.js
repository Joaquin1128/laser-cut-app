import React, { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import Header from './Header';
import './PaymentResultPage.css';

/**
 * Página de resultado de pago de Mercado Pago
 * 
 * Esta página se muestra cuando el usuario retorna de Mercado Pago
 * después de completar o cancelar el pago
 */
function PaymentResultPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const status = searchParams.get('status') || 'unknown';
  const paymentId = searchParams.get('payment_id');
  const preferenceId = searchParams.get('preference_id');

  useEffect(() => {
    // El webhook de MP ya debería haber actualizado el estado del pedido
    // Aquí solo mostramos el resultado al usuario
  }, []);

  const handleGoToOrders = () => {
    navigate('/orders');
  };

  const handleContinueShopping = () => {
    navigate('/');
  };

  const isSuccess = status === 'approved';
  const isPending = status === 'pending' || status === 'in_process';
  const isFailure = status === 'rejected' || status === 'cancelled';

  return (
    <div className="payment-result-page">
      <Header />
      <div className="payment-result-container">
        <div className="payment-result-content">
          {isSuccess && (
            <>
              <div className="payment-result-icon success">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M20 6L9 17l-5-5" />
                </svg>
              </div>
              <h2 className="payment-result-title">¡Pago aprobado!</h2>
              <p className="payment-result-message">
                Tu pedido ha sido procesado exitosamente. Podés ver el estado en "PEDIDOS".
              </p>
              {paymentId && (
                <p className="payment-result-id">
                  ID de pago: {paymentId}
                </p>
              )}
            </>
          )}

          {isPending && (
            <>
              <div className="payment-result-icon pending">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <circle cx="12" cy="12" r="10" />
                  <path d="M12 6v6l4 2" />
                </svg>
              </div>
              <h2 className="payment-result-title">Pago pendiente</h2>
              <p className="payment-result-message">
                Tu pago está siendo procesado. Te notificaremos cuando se confirme.
              </p>
              {paymentId && (
                <p className="payment-result-id">
                  ID de pago: {paymentId}
                </p>
              )}
            </>
          )}

          {isFailure && (
            <>
              <div className="payment-result-icon failure">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <circle cx="12" cy="12" r="10" />
                  <path d="M15 9l-6 6M9 9l6 6" />
                </svg>
              </div>
              <h2 className="payment-result-title">Pago no completado</h2>
              <p className="payment-result-message">
                El pago no pudo ser procesado. Podés intentar nuevamente desde "PEDIDOS".
              </p>
            </>
          )}

          <div className="payment-result-actions">
            <button
              className="btn-secondary"
              type="button"
              onClick={handleContinueShopping}
            >
              COTIZAR MÁS PIEZAS
            </button>
            <button
              className="btn-primary"
              type="button"
              onClick={handleGoToOrders}
            >
              VER MIS PEDIDOS
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default PaymentResultPage;
