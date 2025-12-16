import React, { useEffect, useMemo, useState, useCallback } from 'react';
import './Step.css';
import { getCatalogo } from '../services/api';
import metalIcon from '../assets/icons/stainless.jpg';
import inoxIcon from '../assets/icons/stainless-steel.jpg';

function Step3({ wizardState, onNext, onBack, setHeaderControls }) {
  const {
    material,
    setMaterial,
    thickness,
    setThickness,
    finish,
    setFinish,
    setError,
  } = wizardState;

  const [catalog, setCatalog] = useState([]);
  const [isLoadingCatalog, setIsLoadingCatalog] = useState(false);
  const [isSelectingThickness, setIsSelectingThickness] = useState(false);
  const [isSelectingFinish, setIsSelectingFinish] = useState(false);

  useEffect(() => {
    let isMounted = true;
    const fetchCatalog = async () => {
      try {
        setIsLoadingCatalog(true);
        const data = await getCatalogo();
        if (isMounted) {
          setCatalog(Array.isArray(data) ? data : []);
        }
      } catch (err) {
        setError && setError(err.message || 'No se pudo cargar el catálogo');
      } finally {
        if (isMounted) setIsLoadingCatalog(false);
      }
    };
    fetchCatalog();
    return () => {
      isMounted = false;
    };
  }, [setError]);

  const selectedMaterial = useMemo(
    () => catalog.find((m) => m.nombre === material) || null,
    [catalog, material]
  );

  const sortedEspesores = useMemo(() => {
    if (!selectedMaterial) return [];
    return [...(selectedMaterial.espesores || [])].sort(
      (a, b) => Number(a.espesorMm) - Number(b.espesorMm)
    );
  }, [selectedMaterial]);

  const selectedEspesor = useMemo(() => {
    if (!selectedMaterial || !thickness) return null;
    return (selectedMaterial.espesores || []).find((e) => String(e.espesorMm) === String(thickness)) || null;
  }, [selectedMaterial, thickness]);

  const canContinue = Boolean(
    material &&
    thickness &&
    (!selectedEspesor ||
      !Array.isArray(selectedEspesor.terminaciones) ||
      selectedEspesor.terminaciones.length === 0 ||
      finish)
  );
  const handleSelectMaterial = useCallback((nombre) => {
    setMaterial(nombre);
    setThickness('');
    setFinish && setFinish(null);
    setIsSelectingThickness(true);
  }, [setMaterial, setThickness, setFinish]);

  useEffect(() => {
    setIsSelectingThickness(Boolean(material));
  }, []);

  const handleBackToMaterials = useCallback(() => {
    setIsSelectingThickness(false);
    setIsSelectingFinish(false);
    setMaterial('');
    setThickness('');
    setFinish && setFinish(null);
  }, [setMaterial, setThickness, setFinish]);

  const handleBackToThickness = useCallback(() => {
    setIsSelectingFinish(false);
    setFinish && setFinish(null);
  }, [setFinish]);

  useEffect(() => {
    setHeaderControls({
      showBack: true,
      showNext: true,
      canContinue,
      onNext,
      onBack,
    });
  }, [setHeaderControls, onNext, onBack, canContinue]);

  return (
    <div className="step">
      <h3 className="step-title">¿Con qué material querés trabajar?</h3>
      <p className="step-description">
        {!isSelectingThickness && 'Indicá el material con el que querés trabajar.'}
        {isSelectingThickness && !isSelectingFinish && selectedMaterial && 'Indicá el espesor correspondiente al material elegido.'}
        {isSelectingFinish && 'Indicá la terminación correspondiente al material y espesor elegido.'}
      </p>

      {!isSelectingThickness && (
        <div className="material-categories">
          <div className="category-list">
            {isLoadingCatalog && (
              <div className="loading">
                <div className="spinner"></div>
                <p>Cargando catálogo...</p>
              </div>
            )}
            {!isLoadingCatalog && catalog.map((m) => (
              <button
                key={m.id}
                className="category-card"
                onClick={() => handleSelectMaterial(m.nombre)}
              >
                <div className="category-icon">
                  <img
                    src={m.nombre?.toLowerCase().includes('inoxidable') ? inoxIcon : metalIcon}
                    alt=""
                  />
                </div>
                <div className="category-info">
                  <h4 className="category-title">{m.nombre}</h4>
                  <p className="category-desc">
                    Densidad: {m.densidad} g/cm³
                  </p>
                </div>
              </button>
            ))}
          </div>
        </div>
      )}

      {isSelectingThickness && !isSelectingFinish && selectedMaterial && (
        <div className="material-selection">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <button type="button" className="btn-link" onClick={handleBackToMaterials}>
              ← CAMBIAR MATERIAL
            </button>
          </div>
          <div className="thickness-list">
            {sortedEspesores.map((e) => (
              (() => {
                const hasFinishes = Array.isArray(e.terminaciones) && e.terminaciones.length > 0;
                if (hasFinishes) {
                  return (
                    <button
                      type="button"
                      key={e.id}
                      className="thickness-option"
                      onClick={() => {
                        setThickness(String(e.espesorMm));
                        setFinish && setFinish(null);
                        setIsSelectingFinish(true);
                      }}
                    >
                      <span>
                        {e.espesorMm} MM {e.espesorInch ? `(${e.espesorInch} IN)` : ''}
                      </span>
                    </button>
                  );
                }
                return (
                  <label key={e.id} className="thickness-option">
                    <input
                      type="radio"
                      name="thickness"
                      value={e.espesorMm}
                      checked={String(thickness) === String(e.espesorMm)}
                      onChange={(ev) => {
                        setThickness(ev.target.value);
                        setFinish && setFinish(null);
                      }}
                    />
                    <span>
                      {e.espesorMm} MM {e.espesorInch ? `(${e.espesorInch} IN)` : ''}
                    </span>
                  </label>
                );
              })()
            ))}
          </div>
        </div>
      )}

      {isSelectingFinish && selectedEspesor && Array.isArray(selectedEspesor.terminaciones) && selectedEspesor.terminaciones.length > 0 && (
        <div className="material-selection">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <button type="button" className="btn-link" onClick={handleBackToThickness}>
              ← CAMBIAR ESPESOR
            </button>
          </div>
          <div className="thickness-list">
            {selectedEspesor.terminaciones.map((t) => (
              <label key={t.id} className="thickness-option">
                <input
                  type="radio"
                  name="finish"
                  value={t.nombre}
                  checked={String(finish || '') === String(t.nombre)}
                  onChange={(ev) => setFinish && setFinish(ev.target.value)}
                />
                <span>
                  {t.nombre}
                </span>
              </label>
            ))}
          </div>
        </div>
      )}

    </div>
  );
}

export default Step3;
