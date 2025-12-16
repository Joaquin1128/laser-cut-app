import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDropzone } from 'react-dropzone';
import { FaLock, FaFileUpload, FaIndustry, FaShippingFast } from 'react-icons/fa';
import './UploadPage.css';
import dxfIcon from '../assets/icons/dxf.png';
import { analizarArchivo } from '../services/api';
import ErrorModal from './ErrorModal';
import { simulateProcessingWithProgress } from '../utils/processingSimulator';
import Header from './Header';
import ProcessingModal from './ProcessingModal';

function UploadPage() {
  const navigate = useNavigate();
  const [isProcessing, setIsProcessing] = useState(false);
  const [error, setError] = useState(null);
  const [progress, setProgress] = useState(0);

  const onDrop = async (acceptedFiles) => {
    if (!acceptedFiles || acceptedFiles.length === 0) return;
    const selectedFile = acceptedFiles[0];
    if (!selectedFile.name.toLowerCase().endsWith('.dxf')) {
      setError('Por favor, suba un archivo DXF válido.');
      return;
    }
    setIsProcessing(true);
    setError(null);
    
    try {
      const apiPromise = analizarArchivo(selectedFile);
      
      const data = await simulateProcessingWithProgress(apiPromise, setProgress, {
        minProcessingTime: 2000,
      });
      
      navigate('/wizard', {
        state: {
          file: selectedFile,
          fileData: data
        }
      });
    } catch (err) {
      setError(err.message || 'Error al analizar el archivo.');
      setIsProcessing(false);
    }
  };

  const handleFileBrowse = () => {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.dxf';
    input.onchange = (e) => {
      if (e.target.files && e.target.files.length > 0) {
        onDrop(e.target.files);
      }
    };
    input.click();
  };

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'application/dxf': ['.dxf']
    },
    multiple: false,
    noClick: true,
    noKeyboard: true
  });

  return (
    <div className="upload-page">
      <Header />

      <div className="landing-content-wrapper">
        <div {...getRootProps()} className={`dropzone-container ${isDragActive ? 'drag-active' : ''} ${isProcessing ? 'disabled' : ''}`}>
          <input {...getInputProps()} />

          <div className="file-type-icons">
            <img src={dxfIcon} alt="DXF" className="file-type-icon" draggable={false} />
          </div>

          <div className="dropzone-main-text">
            Arrastra un archivo aquí para comenzar
          </div>

          <div className="dropzone-divider">o</div>

          <button
            className="btn-browse-files"
            onClick={(e) => {
              e.stopPropagation();
              if (!isProcessing) handleFileBrowse();
            }}
            disabled={isProcessing}
          >
            BUSCAR ARCHIVO
          </button>

          <div className="security-message">
            <FaLock className="security-icon" />
            <span>
              <strong>¡Tu diseño está en buenas manos!</strong> Tus archivos se tratarán con confidencialidad, y conservarás todos tus derechos de propiedad intelectual.
            </span>
          </div>
        </div>

        <div className="process-steps">
          <div className="process-step">
            <FaFileUpload className="process-step-icon" />
            <h3 className="process-step-title">Subí tu diseño</h3>
            <p className="process-step-description">
              Cargá tu archivo 2D, elegí material y cantidad. Recibí la cotización al instante.
            </p>
          </div>

          <div className="process-step">
            <FaIndustry className="process-step-icon" />
            <h3 className="process-step-title">Confirmá tu pedido</h3>
            <p className="process-step-description">
              Revisá los detalles y confirmá tu orden. Comenzamos la producción de inmediato.
            </p>
          </div>

          <div className="process-step">
            <FaShippingFast className="process-step-icon" />
            <h3 className="process-step-title">Recibí tus piezas</h3>
            <p className="process-step-description">
              Enviamos tus piezas con control de calidad y seguimiento de envío en tiempo real.
            </p>
          </div>
        </div>
      </div>

      {isProcessing && (
        <ProcessingModal
          progress={progress}
          title="Procesando archivo..."
          message="Estamos analizando tu archivo DXF. Esto puede demorar unos segundos."
        />
      )}

      {error && (
        <ErrorModal
          message={error}
          onClose={() => setError(null)}
        />
      )}
    </div>
  );
}

export default UploadPage;
