const API_BASE_URL = 'http://localhost:8080/api';

const TOKEN_KEY = 'laser_cut_auth_token';

/**
 * Servicio de autenticación
 * 
 * PREPARACIÓN FUTURA:
 * - Cuando se implemente el sistema de pedidos, aquí se agregará:
 *   getPedidos() - Obtener historial de pedidos del usuario
 *   crearPedido(pedidoData) - Crear nuevo pedido vinculado al usuario
 * 
 * INTEGRACIÓN MERCADO PAGO:
 * - Cuando se integre MP, aquí se agregará:
 *   crearPreferenciaPago(pedidoId) - Crear preferencia de pago en MP
 *   procesarPagoWebhook(webhookData) - Procesar webhook de MP
 */
export const authService = {
  getToken: () => {
    return localStorage.getItem(TOKEN_KEY);
  },

  setToken: (token) => {
    localStorage.setItem(TOKEN_KEY, token);
  },

  removeToken: () => {
    localStorage.removeItem(TOKEN_KEY);
  },

  getAuthHeaders: () => {
    const token = authService.getToken();
    return {
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` }),
    };
  },

  async register(nombre, email, password, confirmPassword) {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/register`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          nombre,
          email,
          password,
          confirmPassword,
        }),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => null);
        // Manejar errores de validación
        if (errorData?.errors) {
          const errorMessages = Object.values(errorData.errors).join(', ');
          throw new Error(errorMessages);
        }
        throw new Error(errorData?.message || `Error ${response.status}: ${response.statusText}`);
      }

      const data = await response.json();
      
      // Guardar token
      if (data.token) {
        authService.setToken(data.token);
      }

      return data;
    } catch (error) {
      console.error('Error al registrar usuario:', error);
      throw error;
    }
  },

  async login(email, password) {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email,
          password,
        }),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => null);
        // Manejar errores de validación
        if (errorData?.errors) {
          const errorMessages = Object.values(errorData.errors).join(', ');
          throw new Error(errorMessages);
        }
        throw new Error(errorData?.message || `Error ${response.status}: ${response.statusText}`);
      }

      const data = await response.json();
      
      // Guardar token
      if (data.token) {
        authService.setToken(data.token);
      }

      return data;
    } catch (error) {
      console.error('Error al iniciar sesión:', error);
      throw error;
    }
  },

  async getCurrentUser() {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/me`, {
        method: 'GET',
        headers: authService.getAuthHeaders(),
      });

      if (!response.ok) {
        if (response.status === 401) {
          authService.removeToken();
          throw new Error('Sesión expirada');
        }
        const errorData = await response.json().catch(() => null);
        throw new Error(errorData?.message || `Error ${response.status}: ${response.statusText}`);
      }

      const data = await response.json();
      return data;
    } catch (error) {
      console.error('Error al obtener usuario actual:', error);
      throw error;
    }
  },

  logout: () => {
    authService.removeToken();
  },
};
