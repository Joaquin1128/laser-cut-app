import { authService } from './authService';

const API_BASE_URL = 'http://localhost:8080/api';

/**
 * Servicio de pedidos
 * 
 * PREPARACIÓN INTEGRACIÓN MERCADO PAGO:
 * - Cuando se integre MP, aquí se agregará:
 *   crearPreferenciaPago(pedidoId) - Crear preferencia de pago en MP
 *   consultarEstadoPago(pedidoId) - Consultar estado del pago
 */
export const ordersService = {
  async crearPedido(pedidoData) {
    try {
      const response = await fetch(`${API_BASE_URL}/orders`, {
        method: 'POST',
        headers: authService.getAuthHeaders(),
        body: JSON.stringify(pedidoData),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => null);
        throw new Error(errorData?.message || `Error ${response.status}: ${response.statusText}`);
      }

      const data = await response.json();
      return data;
    } catch (error) {
      console.error('Error al crear pedido:', error);
      throw error;
    }
  },

  async obtenerPedidos() {
    try {
      const response = await fetch(`${API_BASE_URL}/orders`, {
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
      console.error('Error al obtener pedidos:', error);
      throw error;
    }
  },

  async obtenerPedidoPorId(id) {
    try {
      const response = await fetch(`${API_BASE_URL}/orders/${id}`, {
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
      console.error('Error al obtener pedido:', error);
      throw error;
    }
  },

  async crearPreferenciaPago(pedidoId, urls) {
    try {
      const response = await fetch(`${API_BASE_URL}/orders/${pedidoId}/create-preference`, {
        method: 'POST',
        headers: authService.getAuthHeaders(),
        body: JSON.stringify(urls),
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
      console.error('Error al crear preferencia de pago:', error);
      throw error;
    }
  },

  // ========== MÉTODOS DE CHECKOUT ==========

  /**
   * Iniciar checkout - crea un pedido en estado PENDING_CHECKOUT
   */
  async iniciarCheckout(pedidoData) {
    try {
      const response = await fetch(`${API_BASE_URL}/orders/checkout/start`, {
        method: 'POST',
        headers: authService.getAuthHeaders(),
        body: JSON.stringify(pedidoData),
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
      console.error('Error al iniciar checkout:', error);
      throw error;
    }
  },

  /**
   * Actualizar datos de facturación del pedido
   */
  async actualizarFacturacion(pedidoId, billingData) {
    try {
      const response = await fetch(`${API_BASE_URL}/orders/checkout/${pedidoId}/billing`, {
        method: 'PUT',
        headers: authService.getAuthHeaders(),
        body: JSON.stringify(billingData),
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
      console.error('Error al actualizar facturación:', error);
      throw error;
    }
  },

  /**
   * Actualizar datos de envío del pedido
   */
  async actualizarEnvio(pedidoId, shippingData) {
    try {
      const response = await fetch(`${API_BASE_URL}/orders/checkout/${pedidoId}/shipping`, {
        method: 'PUT',
        headers: authService.getAuthHeaders(),
        body: JSON.stringify(shippingData),
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
      console.error('Error al actualizar envío:', error);
      throw error;
    }
  },

  /**
   * Calcular costo de envío (cotización)
   */
  async calcularEnvio(quoteRequest) {
    try {
      const response = await fetch(`${API_BASE_URL}/orders/checkout/calculate-shipping`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...authService.getAuthHeaders(),
        },
        body: JSON.stringify(quoteRequest),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => null);
        throw new Error(errorData?.message || `Error ${response.status}: ${response.statusText}`);
      }

      const data = await response.json();
      return data;
    } catch (error) {
      console.error('Error al calcular envío:', error);
      throw error;
    }
  },

  /**
   * Actualizar costo de envío del pedido
   */
  async actualizarCostoEnvio(pedidoId, shippingCost) {
    try {
      const response = await fetch(`${API_BASE_URL}/orders/checkout/${pedidoId}/shipping-cost`, {
        method: 'PUT',
        headers: authService.getAuthHeaders(),
        body: JSON.stringify({ shippingCost }),
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
      console.error('Error al actualizar costo de envío:', error);
      throw error;
    }
  },

  /**
   * Preparar para pago - cambia estado a PENDING_PAYMENT
   */
  async prepararPago(pedidoId) {
    try {
      const response = await fetch(`${API_BASE_URL}/orders/checkout/${pedidoId}/prepare-payment`, {
        method: 'POST',
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
      console.error('Error al preparar pago:', error);
      throw error;
    }
  },
};
