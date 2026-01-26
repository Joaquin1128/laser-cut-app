import React, { useEffect } from 'react';
import arcosAbiertosImg from '../../assets/icons/arcos_abiertos.png';
import '../ErrorModal.css';

function ErrorModal({ title = 'Ocurrió un error', message, onClose }) {
  useEffect(() => {
    if (!message) {
      return;
    }

    const handleKeyDown = (event) => {
      if (event.key === 'Escape') {
        onClose?.();
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [message, onClose]);

  if (!message) {
    return null;
  }

  const handleOverlayClick = (event) => {
    if (event.target === event.currentTarget) {
      onClose?.();
    }
  };

  // Detectar si es el error de arcos abiertos
  const isArcosAbiertosError = message && message.toLowerCase().includes('arcos abiertos');

  // Solo cambiar el título cuando es el error de arcos abiertos
  // El mensaje viene directamente del backend
  const displayTitle = isArcosAbiertosError ? 'No se puede procesar el archivo DXF' : title;

  return (
    <div
      className="error-modal-overlay"
      role="alertdialog"
      aria-modal="true"
      onMouseDown={handleOverlayClick}
    >
      <div
        className={`error-modal ${isArcosAbiertosError ? 'error-modal-with-image' : ''}`}
        role="document"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <button
          className="error-modal-close"
          type="button"
          onClick={() => onClose?.()}
          aria-label="Cerrar"
        >
          ×
        </button>
        <div className={`error-modal-icon ${isArcosAbiertosError ? 'error-modal-icon-image' : ''}`}>
          {isArcosAbiertosError ? (
            <img 
              src={arcosAbiertosImg} 
              alt="Arcos abiertos" 
              className="error-modal-image"
            />
          ) : (
            <svg 
              className="error-modal-icon-svg" 
              viewBox="0 0 24 24" 
              fill="none" 
              xmlns="http://www.w3.org/2000/svg"
            >
              <circle cx="12" cy="12" r="10" fill="currentColor" fillOpacity="0.1" />
              <path 
                d="M12 8V12M12 16H12.01" 
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
          )}
        </div>
        <h2 className="error-modal-title">{displayTitle}</h2>
        <p className="error-modal-message">{message}</p>
        <button
          className="error-modal-action"
          type="button"
          onClick={() => onClose?.()}
        >
          ENTENDIDO
        </button>
      </div>
    </div>
  );
}

export default ErrorModal;
