import { authService } from './authService';

const API_BASE_URL = 'http://localhost:8080/api';

/**
 * Servicio API para comunicación con el backend
 * 
 * PREPARACIÓN FUTURA:
 * - Cuando se implemente el sistema de pedidos, aquí se agregará:
 *   getPedidos() - Obtener historial de pedidos del usuario
 *   getPedidoById(id) - Obtener detalles de un pedido
 *   crearPedido(pedidoData) - Crear nuevo pedido
 * 
 * INTEGRACIÓN MERCADO PAGO:
 * - Cuando se integre MP, aquí se agregará:
 *   crearPreferenciaPago(pedidoId) - Crear preferencia de pago en MP
 *   procesarPagoWebhook(webhookData) - Procesar webhook de MP
 */

export async function getCatalogo() {
  try {
    const response = await fetch(`${API_BASE_URL}/catalogo`, {
      method: 'GET',
      headers: authService.getAuthHeaders(),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => null);
      throw new Error(errorData?.message || `Error ${response.status}: ${response.statusText}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Error al cargar catálogo:', error);
    throw error;
  }
}

export async function analizarArchivo(file) {
  try {
    const formData = new FormData();
    formData.append('archivo', file);

    const headers = authService.getAuthHeaders();
    // FormData no necesita Content-Type, el navegador lo establece automáticamente
    delete headers['Content-Type'];

    const response = await fetch(`${API_BASE_URL}/analizar-archivo`, {
      method: 'POST',
      headers: headers,
      body: formData,
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => null);
      throw new Error(errorData?.message || `Error ${response.status}: ${response.statusText}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Error al analizar archivo:', error);
    throw error;
  }
}

export async function calcularCotizacion({ archivo, material, espesor, cantidad, unidad }) {
  try {
    const formData = new FormData();
    formData.append('archivo', archivo);
    formData.append('espesor', espesor);
    formData.append('material', material);
    formData.append('cantidad', cantidad);
    formData.append('unidad', unidad);

    const headers = authService.getAuthHeaders();
    // FormData no necesita Content-Type
    delete headers['Content-Type'];

    const response = await fetch(`${API_BASE_URL}/cotizacion`, {
      method: 'POST',
      headers: headers,
      body: formData,
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => null);
      throw new Error(errorData?.message || `Error ${response.status}: ${response.statusText}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Error al calcular cotización:', error);
    throw error;
  }
}
