import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './CartModal.css';

function CartModal({ onClose }) {
  const navigate = useNavigate();

  useEffect(() => {
    const handleKeyDown = (event) => {
      if (event.key === 'Escape') {
        onClose?.();
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [onClose]);

  const handleGoToCart = () => {
    onClose?.();
    navigate('/cart');
  };

  const handleOverlayClick = (event) => {
    if (event.target === event.currentTarget) {
      onClose?.();
    }
  };

  return (
    <div
      className="cart-modal-overlay"
      role="alertdialog"
      aria-modal="true"
      onMouseDown={handleOverlayClick}
    >
      <div
        className="cart-modal"
        role="document"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <button
          className="cart-modal-close"
          type="button"
          onClick={() => onClose?.()}
          aria-label="Cerrar"
        >
          ×
        </button>
        <div className="cart-modal-icon">
          <svg 
            className="cart-modal-icon-svg" 
            viewBox="0 0 24 24" 
            fill="none" 
            xmlns="http://www.w3.org/2000/svg"
          >
            <circle cx="12" cy="12" r="10" fill="currentColor" fillOpacity="0.1" />
            <path 
              d="M9 12L11 14L15 10" 
              stroke="currentColor" 
              strokeWidth="2" 
              strokeLinecap="round" 
              strokeLinejoin="round"
            />
            <circle 
              cx="12" 
              cy="12" 
              r="10" 
              stroke="currentColor" 
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
        </div>
        <h2 className="cart-modal-title">La pieza fue añadida a tu carrito.</h2>
        <div className="cart-modal-actions">
          <button
            className="cart-modal-btn-secondary"
            type="button"
            onClick={() => onClose?.()}
          >
            SEGUIR COTIZANDO
          </button>
          <button
            className="cart-modal-btn-primary"
            type="button"
            onClick={handleGoToCart}
          >
            IR AL CARRITO
          </button>
        </div>
      </div>
    </div>
  );
}

export default CartModal;
