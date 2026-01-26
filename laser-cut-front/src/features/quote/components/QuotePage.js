import React, { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import '../QuotePage.css';
import '../Step.css';
import '../Wizard.css';
import Header from '../../../shared/components/Header';
import Preview from '../../../shared/components/Preview';
import CartModal from '../../cart/components/CartModal';
import { useCart } from '../../../context/CartContext';

function QuotePage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { addToCart } = useCart();
  const [showCartModal, setShowCartModal] = useState(false);
  
  const { quoteData, fileData, file, material, thickness, finish, quantity } = location.state || {};

  useEffect(() => {
    if (!quoteData) {
      navigate('/upload', { replace: true });
    }
  }, [quoteData, navigate]);

  if (!quoteData) {
    return null;
  }

  const handleNewQuote = () => {
    navigate('/upload');
  };

  const handleAddToCart = () => {
    let previewUrl = null;
    if (fileData?.vistaPreviaBase64) {
      try {
        const decodedSvg = atob(fileData.vistaPreviaBase64);
        previewUrl = `data:image/svg+xml;base64,${fileData.vistaPreviaBase64}`;
      } catch (e) {
        console.error('Error generating preview URL:', e);
      }
    }
    
    const cartItem = {
      archivo: {
        nombre: file?.name || 'Sin nombre',
        dimensiones: `${quoteData.ancho} × ${quoteData.alto} mm`,
        urlPreview: previewUrl,
      },
      material: {
        nombre: quoteData.material || '--',
        espesor: quoteData.espesor || 0,
      },
      terminacion: finish || null,
      cantidad: quoteData.cantidad || 1,
      precioUnitario: quoteData.precioUnitario || (quoteData.precioTotal / (quoteData.cantidad || 1)),
      precioTotal: quoteData.precioTotal || 0,
    };

    addToCart(cartItem);
    setShowCartModal(true);
  };

  return (
    <div className="quote-page">
      <Header />

      <div className="quote-page-container">
        <div className="quote-content wizard-step-material">
          <div className="step">
            <h3 className="step-title">Detalle de tu cotización</h3>
            <p className="step-description">Revisá los detalles de tu cotización.</p>

            <div className="summary-card">
              <div className="summary-row">
                <span className="summary-label">Archivo</span>
                <span className="summary-value">{file ? (file.name || 'Cargado') : '--'}</span>
              </div>
              <div className="summary-row">
                <span className="summary-label">Material</span>
                <span className="summary-value">{quoteData.material}</span>
              </div>
              <div className="summary-row">
                <span className="summary-label">Dimensiones</span>
                <span className="summary-value">{quoteData.ancho} × {quoteData.alto} mm</span>
              </div>
              <div className="summary-row">
                <span className="summary-label">Espesor</span>
                <span className="summary-value">{quoteData.espesor} mm</span>
              </div>
              {finish && (
                <div className="summary-row">
                  <span className="summary-label">Terminación</span>
                  <span className="summary-value">{finish}</span>
                </div>
              )}
              <div className="summary-row summary-row-before-total">
                <span className="summary-label">Cantidad</span>
                <span className="summary-value">{quoteData.cantidad} unidades</span>
              </div>
              <div className="summary-row summary-total">
                <span className="summary-label">Total</span>
                <span className="summary-value highlight">${quoteData.precioTotal.toFixed(2)}</span>
              </div>
            </div>

            <div className="quote-actions">
              <button
                type="button"
                className="btn-secondary"
                onClick={handleNewQuote}
              >
                NUEVA COTIZACIÓN
              </button>
              <button
                type="button"
                className="btn-primary"
                onClick={handleAddToCart}
              >
                AÑADIR AL CARRITO
              </button>
            </div>
          </div>
        </div>

        <div className="quote-preview">
          <Preview fileData={fileData} quoteData={quoteData} currentStep={5} thickness={quoteData?.espesor} />
        </div>
      </div>

      {showCartModal && (
        <CartModal onClose={() => setShowCartModal(false)} />
      )}
    </div>
  );
}

export default QuotePage;
