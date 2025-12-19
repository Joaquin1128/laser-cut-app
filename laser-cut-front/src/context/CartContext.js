import React, { createContext, useContext, useState, useEffect } from 'react';
import { cartService } from '../services/cartService';

/**
 * CartContext - Contexto global del carrito
 * 
 * PREPARACIÓN FUTURA:
 * - Cuando se implemente el sistema de pedidos, aquí se vincularán los items del carrito al usuario
 * - Se agregará método: crearPedido() que guardará el carrito como pedido en la BD
 * - Los pedidos se vincularán al usuario autenticado
 * 
 * INTEGRACIÓN MERCADO PAGO:
 * - Cuando se integre MP, aquí se procesará el pago del carrito
 * - Se agregará método: procesarPago() que creará la preferencia en MP
 * - Se redirigirá al checkout de Mercado Pago
 */
const CartContext = createContext();

export const useCart = () => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart must be used within a CartProvider');
  }
  return context;
};

export const CartProvider = ({ children }) => {
  const [cartItems, setCartItems] = useState([]);
  const [isInitialized, setIsInitialized] = useState(false);

  useEffect(() => {
    const savedCart = cartService.getCart();
    setCartItems(savedCart);
    setIsInitialized(true);
  }, []);

  useEffect(() => {
    if (isInitialized) {
      cartService.saveCart(cartItems);
    }
  }, [cartItems, isInitialized]);

  const addToCart = (item) => {
    const cartItem = {
      id: item.id || `cart-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
      archivo: item.archivo,
      material: item.material,
      terminacion: item.terminacion || null,
      cantidad: item.cantidad || 1,
      precioUnitario: item.precioUnitario || 0,
      precioTotal: item.precioTotal || 0,
    };
    setCartItems((prevItems) => [...prevItems, cartItem]);
    return cartItem;
  };

  const removeFromCart = (id) => {
    setCartItems((prevItems) => prevItems.filter((item) => item.id !== id));
  };

  const clearCart = () => {
    setCartItems([]);
  };

  const getCartItemCount = () => {
    return cartItems.length;
  };

  const getCartTotal = () => {
    return cartItems.reduce((total, item) => total + item.precioTotal, 0);
  };

  const value = {
    cartItems,
    addToCart,
    removeFromCart,
    clearCart,
    getCartItemCount,
    getCartTotal,
  };

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
};
