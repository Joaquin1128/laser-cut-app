import React, { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '../services/authService';

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

/**
 * AuthProvider - Contexto global de autenticación
 * 
 * PREPARACIÓN FUTURA:
 * - Cuando se implemente el sistema de pedidos, aquí se podrá acceder al historial del usuario
 * - Se agregará: const [pedidos, setPedidos] = useState([]);
 * - Método para obtener pedidos: getPedidos()
 * 
 * INTEGRACIÓN MERCADO PAGO:
 * - Cuando se integre MP, aquí se guardará el customer_id de MP
 * - Se agregará: const [mercadoPagoCustomerId, setMercadoPagoCustomerId] = useState(null);
 */
export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Verificar si hay un token guardado al cargar la app
    const token = authService.getToken();
    if (token) {
      // Validar token y obtener información del usuario
      authService.getCurrentUser()
        .then((userData) => {
          setUser(userData);
          setIsAuthenticated(true);
        })
        .catch(() => {
          // Token inválido, limpiar
          authService.logout();
        })
        .finally(() => {
          setIsLoading(false);
        });
    } else {
      setIsLoading(false);
    }
  }, []);

  const login = async (email, password) => {
    try {
      const response = await authService.login(email, password);
      setUser(response.usuario);
      setIsAuthenticated(true);
      return response;
    } catch (error) {
      throw error;
    }
  };

  const register = async (nombre, email, password, confirmPassword) => {
    try {
      const response = await authService.register(nombre, email, password, confirmPassword);
      setUser(response.usuario);
      setIsAuthenticated(true);
      return response;
    } catch (error) {
      throw error;
    }
  };

  const logout = () => {
    authService.logout();
    setUser(null);
    setIsAuthenticated(false);
  };

  const value = {
    user,
    isAuthenticated,
    isLoading,
    login,
    register,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
