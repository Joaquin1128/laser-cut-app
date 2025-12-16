import React, { useState, useMemo } from 'react';
import './Preview.css';
import Preview3D from './Preview3D';

function Preview({ fileData, quoteData, currentStep, thickness }) {
  const [viewMode, setViewMode] = useState('3d'); // '2d' o '3d' - por defecto 3D

  // Espesor efectivo: usar el seleccionado o 3mm por defecto para vista 3D
  const effectiveThickness = thickness || 3;
  // Solo mostrar el SVG, no la cotización (la cotización se muestra en Step4)
  if (quoteData && currentStep === 4) {
    const decodedSvg = fileData && fileData.vistaPreviaBase64 
      ? atob(fileData.vistaPreviaBase64) 
      : null;

    return (
      <div className="preview">
        {decodedSvg && (
          <div className="preview-view-toggle">
            <button
              className={`preview-toggle-btn ${viewMode === '2d' ? 'active' : ''}`}
              onClick={() => setViewMode('2d')}
              type="button"
            >
              2D
            </button>
            <button
              className={`preview-toggle-btn ${viewMode === '3d' ? 'active' : ''}`}
              onClick={() => setViewMode('3d')}
              type="button"
            >
              3D
            </button>
          </div>
        )}
        <div className="preview-content">
          {decodedSvg && (
            <>
              {viewMode === '2d' ? (
            <div className="preview-image-container">
              <div 
                dangerouslySetInnerHTML={{ __html: decodedSvg }}
                className="preview-svg"
              />
            </div>
              ) : (
                <Preview3D svgString={decodedSvg} thickness={effectiveThickness} />
              )}
            </>
          )}
        </div>
      </div>
    );
  }

  if (fileData) {
    const decodedSvg = fileData.vistaPreviaBase64 
      ? atob(fileData.vistaPreviaBase64) 
      : null;

    return (
      <div className="preview">
        {decodedSvg && (
          <div className="preview-view-toggle">
            <button
              className={`preview-toggle-btn ${viewMode === '2d' ? 'active' : ''}`}
              onClick={() => setViewMode('2d')}
              type="button"
            >
              2D
            </button>
            <button
              className={`preview-toggle-btn ${viewMode === '3d' ? 'active' : ''}`}
              onClick={() => setViewMode('3d')}
              type="button"
            >
              3D
            </button>
          </div>
        )}
        <div className="preview-content">
          {decodedSvg ? (
            <>
              {viewMode === '2d' ? (
            <div className="preview-image-container">
              <div 
                dangerouslySetInnerHTML={{ __html: decodedSvg }}
                className="preview-svg"
              />
            </div>
              ) : (
                <Preview3D svgString={decodedSvg} thickness={effectiveThickness} />
              )}
            </>
          ) : (
            <div className="preview-placeholder">
              <div className="preview-icon">📄</div>
              <p>Subí un archivo compatible para ver la vista previa.</p>
            </div>
          )}
        </div>
      </div>
    );
  }

  return (
    <div className="preview">
      <h3 className="preview-title">Preview</h3>
      <div className="preview-content preview-empty">
        <div className="preview-placeholder">
          <div className="preview-icon">📊</div>
          <p>Sube un archivo para ver el preview</p>
        </div>
      </div>
    </div>
  );
}

export default Preview;
