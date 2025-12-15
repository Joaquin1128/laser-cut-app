import React, { useEffect, useState, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import './Step.css';
import { calcularCotizacion } from '../services/api';
import { simulateProcessingWithProgress } from '../utils/processingSimulator';
import ProcessingModal from './ProcessingModal';

function Step4({ wizardState, onBack, setHeaderControls }) {
  const navigate = useNavigate();
  const {
    file,
    fileData,
    unitConfirmed,
    material,
    thickness,
    finish,
    quantity,
    isLoading,
    setIsLoading,
    setError,
    setQuoteData,
    quoteData,
  } = wizardState;

  const [progress, setProgress] = useState(0);

  const canGenerate = Boolean(
    file && fileData && unitConfirmed && material && thickness && quantity > 0 && !isLoading
  );

  const formatDimensionValue = useCallback((value) => {
    if (value === null || value === undefined) return '--';
    const numeric = Number(value);
    if (Number.isNaN(numeric)) return '--';
    return numeric.toFixed(3).replace(/\.?0+$/, '');
  }, []);

  const dimensionsText = useMemo(() => {
    if (!fileData || !unitConfirmed) return '--';
    const ancho = formatDimensionValue(fileData.ancho);
    const alto = formatDimensionValue(fileData.alto);
    return unitConfirmed === 'INCH'
      ? `${ancho}" × ${alto}"`
      : `${ancho} × ${alto} mm`;
  }, [fileData, unitConfirmed, formatDimensionValue]);

  const handleGenerate = useCallback(async () => {
    if (!canGenerate) return;
    setIsLoading(true);
    setError(null);
    
    try {
      const apiPromise = calcularCotizacion({
        archivo: file,
        material,
        espesor: parseFloat(thickness),
        cantidad: parseInt(quantity),
        unidad: unitConfirmed === 'INCH' ? 'INCH' : 'MM',
      });
      
      const data = await simulateProcessingWithProgress(apiPromise, setProgress, {
        minProcessingTime: 2000,
      });
      
      setQuoteData(data);
      
      navigate('/quote', {
        state: {
          quoteData: data,
          fileData,
          file,
          material,
          thickness,
          finish,
          quantity,
        }
      });
    } catch (err) {
      setError(err.message || 'Error al calcular la cotización');
    } finally {
      setIsLoading(false);
    }
  }, [canGenerate, setIsLoading, setError, file, material, thickness, quantity, unitConfirmed, setQuoteData]);

  useEffect(() => {
    const controls = {
      showBack: true,
      onBack,
      showNext: true,
      nextLabel: 'GENERAR COTIZACIÓN',
      canContinue: canGenerate,
      isLoading: isLoading,
      onNext: handleGenerate,
    };

    setHeaderControls(controls);
  }, [setHeaderControls, canGenerate, isLoading, onBack, handleGenerate]);

  return (
    <div className="step">
      <h3 className="step-title">Confirmá tu información</h3>
      <p className="step-description">Revisá que todo esté correcto antes de generar la cotización.</p>

      <div className="summary-card">
        <div className="summary-row">
          <span className="summary-label">Archivo</span>
          <span className="summary-value">{file ? (file.name || 'Cargado') : '--'}</span>
        </div>
        <div className="summary-row">
          <span className="summary-label">Material</span>
          <span className="summary-value">{material || '--'}</span>
        </div>
        <div className="summary-row">
          <span className="summary-label">Dimensiones</span>
          <span className="summary-value">{dimensionsText}</span>
        </div>
        <div className="summary-row">
          <span className="summary-label">Espesor</span>
          <span className="summary-value">{thickness ? `${thickness} mm` : '--'}</span>
        </div>
        {finish && (
          <div className="summary-row">
            <span className="summary-label">Terminación</span>
            <span className="summary-value">{finish}</span>
          </div>
        )}
        <div className="summary-row">
          <span className="summary-label">Cantidad</span>
          <span className="summary-value">{quantity || '--'}</span>
        </div>
      </div>

      {isLoading && (
        <ProcessingModal
          progress={progress}
          title="Generando cotización..."
          message="Estamos calculando el precio de tu cotización. Esto puede tomar unos segundos."
        />
      )}
    </div>
  );
}

export default Step4;
