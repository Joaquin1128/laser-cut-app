import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDropzone } from 'react-dropzone';
import { FaLock, FaUpload } from 'react-icons/fa';
import '../UploadPage.css';
import dxfIcon from '../../../assets/icons/dxf.png';
import { analizarArchivo } from '../../../services/api';
import ErrorModal from '../../../shared/components/ErrorModal';
import { simulateProcessingWithProgress } from '../../../utils/processingSimulator';
import Header from '../../../shared/components/Header';
import ProcessingModal from '../../../shared/components/ProcessingModal';

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
        <div className="timeline-container">
          <div className="timeline-step">
            <div className="timeline-step-number">1</div>
            <div className="timeline-step-content">
              <h3 className="timeline-step-title">Cotizá tu diseño</h3>
              <p className="timeline-step-description">
              Subí tu archivo y seleccioná material, espesor, terminación y cantidad de piezas. Nuestro sistema calculará el costo automáticamente y te mostrará la cotización al instante, para que sepas cuánto vas a pagar antes de continuar.
              </p>
            </div>
          </div>

          <div className="timeline-step">
            <div className="timeline-step-number">2</div>
            <div className="timeline-step-content">
              <h3 className="timeline-step-title">Confirmá tu compra</h3>
              <p className="timeline-step-description">
                Revisá todos los detalles de tu pedido: material, cantidad, espesor y precio final. Cuando estés seguro, agregalo al carrito y completá la compra de manera rápida y segura. Podés corregir cualquier detalle antes de finalizar.
              </p>
            </div>
          </div>

          <div className="timeline-step">
            <div className="timeline-step-number">3</div>
            <div className="timeline-step-content">
              <h3 className="timeline-step-title">Retirá o recibí tus piezas</h3>
              <p className="timeline-step-description">
                Elegí cómo querés recibir tu pedido: retiro en nuestro local o envío según el peso y la cantidad de piezas. Nos aseguramos de que tus piezas lleguen listas para usar, bien protegidas y sin complicaciones.
              </p>
            </div>
          </div>
        </div>

        <div {...getRootProps()} className={`dropzone-container ${isDragActive ? 'drag-active' : ''} ${isProcessing ? 'disabled' : ''}`}>
          <input {...getInputProps()} />

          <div className="file-type-icons">
            <img src={dxfIcon} alt="DXF" className="file-type-icon" draggable={false} />
          </div>

          <div className="dropzone-main-text">
            Arrastrá un archivo aquí para comenzar
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
            <FaUpload className="btn-upload-icon" />
            BUSCAR ARCHIVO
          </button>

          <div className="security-divider"></div>
          <div className="security-message">
            <FaLock className="security-icon" />
            <span>
              <strong>¡Tu diseño está en buenas manos!</strong> Tus archivos se tratarán con confidencialidad, y conservarás todos tus derechos de propiedad intelectual.
            </span>
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
