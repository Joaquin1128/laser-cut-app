import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { FaShoppingCart } from 'react-icons/fa';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import logoG2 from '../assets/icons/logo_g2.png';
import AuthModal from './AuthModal';
import './Header.css';

function Header({ children }) {
  const navigate = useNavigate();
  const { getCartItemCount } = useCart();
  const { isAuthenticated, user, logout } = useAuth();
  const [showAuthModal, setShowAuthModal] = useState(false);
  const cartItemCount = getCartItemCount();

  const handleCartClick = () => {
    navigate('/cart');
  };

  const handleLoginClick = () => {
    setShowAuthModal(true);
  };

  const handleOrdersClick = () => {
    if (isAuthenticated) {
      navigate('/orders');
    } else {
      setShowAuthModal(true);
    }
  };

  const handleLogout = () => {
    logout();
  };

  return (
    <header className="header">
      <div
        className="header-logo"
        onClick={() => navigate('/')}
        style={{ cursor: 'pointer' }}
      >
        <img
          src={logoG2}
          alt="Logo G2"
          className="header-logo-img"
        />
      </div>
      {children && <div className="header-content">{children}</div>}
      <div className="header-right-actions">
        <button
          className="cart-icon-button"
          type="button"
          onClick={handleCartClick}
          aria-label="Ver carrito"
        >
          <FaShoppingCart className="cart-icon" />
          {cartItemCount > 0 && (
            <span className="cart-badge">{cartItemCount}</span>
          )}
        </button>
        <button 
          className="btn-login-header" 
          type="button"
          onClick={handleOrdersClick}
        >
          PEDIDOS
        </button>
        {isAuthenticated ? (
          <div className="header-user-menu">
            <span className="header-user-greeting">Hola, {user?.nombre}</span>
            <button 
              className="btn-login-header" 
              type="button"
              onClick={handleLogout}
            >
              SALIR
            </button>
          </div>
        ) : (
          <button 
            className="btn-login-header" 
            type="button"
            onClick={handleLoginClick}
          >
            INGRESAR
          </button>
        )}
      </div>

      {showAuthModal && (
        <AuthModal onClose={() => setShowAuthModal(false)} />
      )}
    </header>
  );
}

export default Header;
