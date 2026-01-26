import React, { useState, useEffect } from 'react';
import { useAuth } from '../../../context/AuthContext';
import logoG2 from '../../../assets/icons/logo_g2.png';
import '../AuthModal.css';

/**
 * Modal de autenticación (Login/Registro)
 * 
 * PREPARACIÓN FUTURA:
 * - Cuando se implemente el sistema de pedidos, aquí se mostrará un enlace al historial
 * - Se agregará: <Link to="/pedidos">Ver mis pedidos</Link>
 * 
 * INTEGRACIÓN MERCADO PAGO:
 * - Cuando se integre MP, aquí se mostrará información de métodos de pago guardados
 * - Se agregará sección: "Métodos de pago guardados"
 */
function AuthModal({ onClose }) {
  const { login, register } = useAuth();
  const [isLogin, setIsLogin] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  // Estados unificados
  const [nombre, setNombre] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    if (isLogin) {
      // Login
      setIsLoading(true);
      try {
        await login(email, password);
        onClose();
      } catch (err) {
        setError(err.message || 'Error al iniciar sesión');
      } finally {
        setIsLoading(false);
      }
    } else {
      // Registro
      // Validación básica
      if (password !== confirmPassword) {
        setError('Las contraseñas no coinciden');
        return;
      }

      if (password.length < 6) {
        setError('La contraseña debe tener al menos 6 caracteres');
        return;
      }

      setIsLoading(true);
      try {
        await register(nombre, email, password, confirmPassword);
        onClose();
      } catch (err) {
        setError(err.message || 'Error al registrarse');
      } finally {
        setIsLoading(false);
      }
    }
  };

  useEffect(() => {
    const handleKeyDown = (event) => {
      if (event.key === 'Escape') {
        onClose();
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [onClose]);

  const handleOverlayClick = (event) => {
    if (event.target === event.currentTarget) {
      onClose();
    }
  };

  const switchMode = () => {
    setIsLogin(!isLogin);
    setError(null);
    // Limpiar campos al cambiar de modo
    setNombre('');
    setEmail('');
    setPassword('');
    setConfirmPassword('');
  };

  return (
    <div
      className="auth-modal-overlay"
      role="dialog"
      aria-modal="true"
      onMouseDown={handleOverlayClick}
    >
      <div
        className="auth-modal"
        role="document"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <button
          className="auth-modal-close"
          type="button"
          onClick={onClose}
          aria-label="Cerrar"
        >
          ×
        </button>

        <div className="auth-modal-content">
          <div className="auth-modal-logo">
            <img
              src={logoG2}
              alt="Logo G2"
              className="auth-modal-logo-img"
            />
          </div>
          <form onSubmit={handleSubmit} className="auth-form">
            <h2 className="auth-form-title">
              {isLogin ? 'Iniciar sesión' : 'Registrarse'}
            </h2>
            
            {error && (
              <div className="auth-error-message">
                {error}
              </div>
            )}

            {!isLogin && (
              <div className="auth-form-group">
                <input
                  id="nombre"
                  type="text"
                  className="auth-form-input"
                  value={nombre}
                  onChange={(e) => setNombre(e.target.value)}
                  required={!isLogin}
                  disabled={isLoading}
                  placeholder="Nombre"
                />
              </div>
            )}

            <div className="auth-form-group">
              <input
                id="email"
                type="email"
                className="auth-form-input"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                disabled={isLoading}
                placeholder="Email"
              />
            </div>

            <div className="auth-form-group">
              <input
                id="password"
                type="password"
                className="auth-form-input"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                disabled={isLoading}
                placeholder="Contraseña"
                minLength={isLogin ? undefined : 6}
              />
            </div>

            {!isLogin && (
              <div className="auth-form-group">
                <input
                  id="confirmPassword"
                  type="password"
                  className="auth-form-input"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  required={!isLogin}
                  disabled={isLoading}
                  placeholder="Confirmar contraseña"
                  minLength={6}
                />
              </div>
            )}

            <button
              type="submit"
              className="auth-form-submit btn-primary"
              disabled={isLoading}
            >
              {isLoading 
                ? (isLogin ? 'Iniciando sesión...' : 'Registrando usuario...')
                : (isLogin ? 'INICIAR SESIÓN' : 'REGISTRARSE')
              }
            </button>

            {isLogin && (
              <div className="auth-form-forgot-password">
                <button
                  type="button"
                  className="auth-form-forgot-link"
                  onClick={() => {
                    // TODO: Implementar recuperación de contraseña
                    console.log('Recuperar contraseña');
                  }}
                  disabled={isLoading}
                >
                  ¿Olvidaste tu contraseña?
                </button>
              </div>
            )}

            <div className="auth-form-divider">
              <span className="auth-form-divider-line"></span>
            </div>

            <div className="auth-form-footer">
              {isLogin ? (
                <p className="auth-form-footer-text">
                  ¿No tienes una cuenta?{' '}
                  <button
                    type="button"
                    className="auth-form-footer-link"
                    onClick={switchMode}
                    disabled={isLoading}
                  >
                    REGISTRARSE
                  </button>
                </p>
              ) : (
                <p className="auth-form-footer-text">
                  ¿Ya tienes una cuenta?{' '}
                  <button
                    type="button"
                    className="auth-form-footer-link"
                    onClick={switchMode}
                    disabled={isLoading}
                  >
                    INICIAR SESIÓN
                  </button>
                </p>
              )}
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}

export default AuthModal;
