import React from 'react';
import { useNavigate } from 'react-router-dom';
import { FaShoppingCart } from 'react-icons/fa';
import { useCart } from '../context/CartContext';
import logoG2 from '../assets/icons/logo_g2.png';
import './Header.css';

function Header({ children }) {
  const navigate = useNavigate();
  const { getCartItemCount } = useCart();
  const cartItemCount = getCartItemCount();

  const handleCartClick = () => {
    navigate('/cart');
  };

  const handleLoginClick = () => {
    // falta implementar login y usuarios
    console.log('Login clicked');
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
          onClick={handleLoginClick}
        >
          LOGIN
        </button>
      </div>
    </header>
  );
}

export default Header;
