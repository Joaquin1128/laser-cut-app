import React, { useState, useEffect, useCallback } from 'react';
import './Step.css';

function Step3({ wizardState, onNext, onBack, setHeaderControls }) {
  const {
    quantity,
    setQuantity,
  } = wizardState;

  const [tempQuantity, setTempQuantity] = useState(1);
  const MAX_QUANTITY = 30;
  const canContinue = tempQuantity && tempQuantity > 0 && tempQuantity <= MAX_QUANTITY;
  const pressTimerRef = React.useRef(null);
  const pressTypeRef = React.useRef(null);

  useEffect(() => {
    setQuantity(tempQuantity);
  }, [tempQuantity, setQuantity]);

  const stopPress = useCallback(() => {
    if (pressTimerRef.current) {
      clearInterval(pressTimerRef.current);
      pressTimerRef.current = null;
      pressTypeRef.current = null;
    }
  }, []);

  const startPress = useCallback((type) => {
    pressTypeRef.current = type;
    if (pressTimerRef.current) clearInterval(pressTimerRef.current);
    pressTimerRef.current = setInterval(() => {
      setTempQuantity((prev) => {
        const next = type === 'inc' 
          ? Math.min(MAX_QUANTITY, prev + 1) 
          : Math.max(1, prev - 1);
        return next;
      });
    }, 120);
  }, []);

  useEffect(() => {
    return () => stopPress();
  }, [stopPress]);

  const handleInputChange = (e) => {
    const val = e.target.value.replace(/[^\d]/g, '');
    if (val === '') {
      setTempQuantity(1);
      return;
    }
    const num = parseInt(val, 10);
    const clampedNum = Math.min(MAX_QUANTITY, Math.max(1, num));
    setTempQuantity(clampedNum);
  };

  const handleBlur = () => {
    if (!tempQuantity || tempQuantity < 1) {
      setTempQuantity(1);
    } else if (tempQuantity > MAX_QUANTITY) {
      setTempQuantity(MAX_QUANTITY);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'ArrowUp') {
      e.preventDefault();
      setTempQuantity((q) => Math.min(MAX_QUANTITY, q + 1));
    } else if (e.key === 'ArrowDown') {
      e.preventDefault();
      setTempQuantity((q) => Math.max(1, q - 1));
    }
  };

  useEffect(() => {
    setHeaderControls({
      showBack: true,
      showNext: true,
      nextLabel: 'CONFIRMAR',
      canContinue,
      onNext,
      onBack,
    });
  }, [
    setHeaderControls,
    onNext,
    onBack,
    canContinue,
  ]);

  return (
    <div className="step">
      <h3 className="step-title">¿Cuántas piezas querés fabricar?</h3>
      <p className="step-description">Indicá cuántas piezas querés producir. El máximo permitido es 30.</p>

      <div className="quantity-selector">
        <div className="quantity-controls">
          <button
            className="quantity-btn"
            onClick={() => setTempQuantity(Math.max(1, tempQuantity - 1))}
            onMouseDown={() => startPress('dec')}
            onMouseUp={stopPress}
            onMouseLeave={stopPress}
            onTouchStart={() => startPress('dec')}
            onTouchEnd={stopPress}
            disabled={tempQuantity <= 1}
          >
            −
          </button>
          <input
            type="number"
            min="1"
            max={MAX_QUANTITY}
            value={tempQuantity}
            onChange={handleInputChange}
            onBlur={handleBlur}
            onKeyDown={handleKeyDown}
            onWheel={(e) => e.target.blur()}
            className="quantity-input"
          />
          <button
            className="quantity-btn"
            onClick={() => setTempQuantity(Math.min(MAX_QUANTITY, tempQuantity + 1))}
            onMouseDown={() => startPress('inc')}
            onMouseUp={stopPress}
            onMouseLeave={stopPress}
            onTouchStart={() => startPress('inc')}
            onTouchEnd={stopPress}
            disabled={tempQuantity >= MAX_QUANTITY}
          >
            +
          </button>
        </div>
      </div>
    </div>
  );
}

export default Step3;
