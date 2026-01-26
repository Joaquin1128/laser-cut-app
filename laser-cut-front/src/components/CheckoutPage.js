import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import { ordersService } from '../services/ordersService';
import Header from './Header';
import WizardSteps from './WizardSteps';
import ErrorModal from './ErrorModal';
import AuthModal from './AuthModal';
import ProcessingModal from './ProcessingModal';
import CartStep from './checkout/CartStep';
import BillingShippingStep from './checkout/BillingShippingStep';
import SummaryStep from './checkout/SummaryStep';
import './CheckoutPage.css';
import './Wizard.css';
import './Step.css';

const CHECKOUT_STEPS = [
  { label: 'Carrito', summary: 'Revisar items' },
  { label: 'Facturación y envío', summary: 'Facturación y envío' },
  { label: 'Resumen', summary: 'Confirmar y pagar' },
];

function CheckoutPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { cartItems, getCartTotal } = useCart();
  const { isAuthenticated } = useAuth();
  
  const [currentStep, setCurrentStep] = useState(1);
  const [pedidoId, setPedidoId] = useState(null);
  const [pedido, setPedido] = useState(null);
  const [error, setError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [showAuthModal, setShowAuthModal] = useState(false);
  const [isProcessingPayment, setIsProcessingPayment] = useState(false);
  const [paymentProgress, setPaymentProgress] = useState(0);

  // Datos del checkout
  const [billingData, setBillingData] = useState({
    billingName: '',
    billingEmail: '',
    billingType: 'C', // Siempre tipo C
    fiscalId: '', // DNI
    billingPhone: '', // Teléfono
  });

  const [shippingData, setShippingData] = useState({
    shippingType: 'DELIVERY', // Solo envío, no hay retiro
    street: '',
    unit: '', // Piso / Depto / Unidad (opcional)
    city: '',
    postalCode: '',
    province: '',
    country: 'Argentina', // Siempre Argentina, no se muestra en el formulario
  });

  const [shippingCost, setShippingCost] = useState(0);
  const [shippingQuote, setShippingQuote] = useState(null);

  useEffect(() => {
    if (!isAuthenticated) {
      setShowAuthModal(true);
    }
  }, [isAuthenticated]);

  useEffect(() => {
    // Si venimos del carrito y no hay pedido iniciado, iniciar checkout
    if (isAuthenticated && cartItems.length > 0 && !pedidoId && currentStep === 1 && !isLoading) {
      iniciarCheckout();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAuthenticated, cartItems.length]);

  const iniciarCheckout = async () => {
    if (cartItems.length === 0) {
      navigate('/cart');
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      const total = getCartTotal();
      const itemsPedido = cartItems.map((item) => ({
        material: item.material?.nombre || 'Desconocido',
        thickness: item.material?.espesor || 0,
        quantity: item.cantidad || 1,
        unitPrice: item.precioUnitario || 0,
        totalPrice: item.precioTotal || 0,
        metadata: JSON.stringify({
          archivoNombre: item.archivo?.nombre,
          dimensiones: item.archivo?.dimensiones,
          terminacion: item.terminacion,
          urlPreview: item.archivo?.urlPreview,
        }),
      }));

      const pedidoData = {
        totalPrice: total,
        items: itemsPedido,
      };

      const pedidoCreado = await ordersService.iniciarCheckout(pedidoData);
      setPedidoId(pedidoCreado.id);
      setPedido(pedidoCreado);
    } catch (err) {
      console.error('Error al iniciar checkout:', err);
      setError(err.message || 'Error al iniciar el checkout. Por favor, intentá nuevamente.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleNext = async () => {
    if (currentStep === 1) {
      // Paso 1: Carrito - ya está validado
      setCurrentStep(2);
    } else if (currentStep === 2) {
      // Paso 2: Facturación y envío - validar y guardar ambos
      if (!billingData.billingName || !billingData.billingEmail || !billingData.fiscalId || !billingData.billingPhone) {
        setError('Por favor completá todos los campos obligatorios');
        return;
      }

      if (!shippingData.street || !shippingData.city || !shippingData.postalCode || !shippingData.province) {
        setError('Por favor completá todos los campos de dirección');
        return;
      }

      if (shippingCost === 0) {
        setError('Por favor calculá el costo de envío antes de continuar');
        return;
      }

      setIsLoading(true);
      try {
        // Guardar facturación
        let pedidoActualizado = await ordersService.actualizarFacturacion(pedidoId, billingData);
        
        // Guardar envío
        pedidoActualizado = await ordersService.actualizarEnvio(pedidoId, shippingData);
        
        // Actualizar costo de envío si existe
        if (shippingCost > 0) {
          pedidoActualizado = await ordersService.actualizarCostoEnvio(pedidoId, shippingCost);
        }

        setPedido(pedidoActualizado);
        setCurrentStep(3);
        setError(null);
      } catch (err) {
        setError(err.message || 'Error al guardar los datos');
      } finally {
        setIsLoading(false);
      }
    }
  };

  const handleBack = () => {
    if (currentStep > 1) {
      setCurrentStep(currentStep - 1);
      setError(null);
    } else {
      navigate('/cart');
    }
  };

  const handleProceedToPayment = async () => {
    setIsProcessingPayment(true);
    setPaymentProgress(0);

    let progressInterval = null;

    try {
      progressInterval = setInterval(() => {
        setPaymentProgress((prev) => {
          if (prev >= 90) return prev;
          const increment = Math.max(0.5, (90 - prev) * 0.02);
          return Math.min(prev + increment, 90);
        });
      }, 100);

      setPaymentProgress(10);

      // Preparar pedido para pago (cambiar estado a PENDING_PAYMENT)
      await ordersService.prepararPago(pedidoId);
      setPaymentProgress(40);

      // Crear preferencia de Mercado Pago
      const baseUrl = window.location.origin;
      const urls = {
        successUrl: `${baseUrl}/payment/success?status=approved`,
        failureUrl: `${baseUrl}/payment/failure?status=rejected`,
        pendingUrl: `${baseUrl}/payment/pending?status=pending`,
      };

      setPaymentProgress(70);
      const preference = await ordersService.crearPreferenciaPago(pedidoId, urls);

      if (progressInterval) {
        clearInterval(progressInterval);
        progressInterval = null;
      }

      setPaymentProgress(100);
      await new Promise(resolve => setTimeout(resolve, 800));

      const checkoutUrl = preference.sandboxInitPoint || preference.initPoint;
      if (checkoutUrl) {
        window.location.href = checkoutUrl;
      } else {
        throw new Error('No se pudo obtener la URL de checkout');
      }
    } catch (err) {
      if (progressInterval) {
        clearInterval(progressInterval);
      }
      console.error('Error al procesar el pago:', err);
      setError(err.message || 'Error al procesar el pago. Por favor, intentá nuevamente.');
      setIsProcessingPayment(false);
      setPaymentProgress(0);
    }
  };

  if (!isAuthenticated) {
    return (
      <div className="checkout-page">
        <Header />
        {showAuthModal && (
          <AuthModal onClose={() => {
            setShowAuthModal(false);
            navigate('/cart');
          }} />
        )}
      </div>
    );
  }

  if (cartItems.length === 0 && !pedidoId) {
    return (
      <div className="checkout-page">
        <Header />
        <div className="checkout-page-container">
          <div className="checkout-empty">
            <h2>Tu carrito está vacío</h2>
            <p>Agregá piezas desde la página de cotización.</p>
            <button className="btn-primary" onClick={() => navigate('/upload')}>
              COTIZAR PIEZAS
            </button>
          </div>
        </div>
      </div>
    );
  }

  const stepLabels = CHECKOUT_STEPS.map(s => s.label);
  const stepSummaries = CHECKOUT_STEPS.map(s => s.summary);

  // Determinar si el botón "Continuar" debe estar habilitado
  const canContinue = () => {
    if (currentStep === 1) {
      return cartItems.length > 0;
    } else if (currentStep === 2) {
      // Validar facturación y envío
      const billingValid = billingData.billingName && billingData.billingEmail && billingData.fiscalId && billingData.billingPhone;
      const shippingValid = shippingData.street && shippingData.city && shippingData.postalCode && shippingData.province;
      const shippingCostValid = shippingCost > 0; // Solo necesita que el costo esté calculado
      return billingValid && shippingValid && shippingCostValid;
    }
    return true;
  };

  // Determinar el label del botón según el step
  const getNextButtonLabel = () => {
    if (currentStep === 3) {
      return isProcessingPayment ? 'PROCESANDO...' : 'PAGAR CON MERCADO PAGO';
    }
    return isLoading ? 'GUARDANDO...' : 'CONTINUAR';
  };

  const handleNextClick = () => {
    if (currentStep === 3) {
      handleProceedToPayment();
    } else {
      handleNext();
    }
  };

  const showNextButton = currentStep !== 1 || cartItems.length > 0;

  return (
    <div className="checkout-page">
      <Header />
      <div className="checkout-wizard-steps">
        <WizardSteps
          stepLabels={stepLabels}
          stepSummaries={stepSummaries}
          activeStep={currentStep}
        />
      </div>
      <div className="checkout-page-container">
        <div className="checkout-step-wrapper">
          <div className="wizard-header-buttons">
            {currentStep > 1 && (
              <button
                type="button"
                className="btn-secondary"
                onClick={handleBack}
                disabled={isLoading || isProcessingPayment}
              >
                VOLVER
              </button>
            )}
            {showNextButton && (
              <button
                type="button"
                className="btn-primary"
                onClick={handleNextClick}
                disabled={isLoading || isProcessingPayment || !canContinue()}
              >
                {getNextButtonLabel()}
              </button>
            )}
          </div>
          <div className="checkout-content">
          {currentStep === 1 && (
            <CartStep
              cartItems={cartItems}
              onNext={handleNext}
              onBack={handleBack}
              isLoading={isLoading}
            />
          )}

          {currentStep === 2 && (
            <BillingShippingStep
              billingData={billingData}
              setBillingData={setBillingData}
              shippingData={shippingData}
              setShippingData={setShippingData}
              shippingCost={shippingCost}
              setShippingCost={setShippingCost}
              shippingQuote={shippingQuote}
              setShippingQuote={setShippingQuote}
              pedidoId={pedidoId}
              onNext={handleNext}
              onBack={handleBack}
              isLoading={isLoading}
            />
          )}

          {currentStep === 3 && (
            <SummaryStep
              pedido={pedido}
              billingData={billingData}
              shippingData={shippingData}
              shippingCost={shippingCost}
              cartItems={cartItems}
              onBack={handleBack}
              onProceedToPayment={handleProceedToPayment}
              isProcessingPayment={isProcessingPayment}
              paymentProgress={paymentProgress}
            />
          )}
        </div>
        </div>
      </div>

      {error && (
        <ErrorModal
          message={error}
          onClose={() => setError(null)}
        />
      )}

      {isProcessingPayment && (
        <ProcessingModal
          progress={paymentProgress}
          title="Preparando tu pago..."
          message="Estamos procesando tu pedido y preparando la redirección a Mercado Pago. Por favor esperá unos segundos."
        />
      )}
    </div>
  );
}

export default CheckoutPage;

