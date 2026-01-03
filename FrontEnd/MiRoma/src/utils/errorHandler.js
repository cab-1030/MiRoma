/**
 * Utilidad para manejar errores de forma segura en el frontend
 * No revela información sensible al usuario
 */

/**
 * Extrae un mensaje de error seguro de una respuesta del servidor
 * @param {Response} response - La respuesta HTTP
 * @param {Object} data - Los datos JSON de la respuesta
 * @returns {string} - Mensaje de error seguro para el usuario
 */
export async function getSafeErrorMessage(response, data) {
  // Si la respuesta tiene un formato ErrorResponse estándar
  if (data && data.message && data.code) {
    return data.message;
  }

  // Si tiene un campo "error" (formato antiguo)
  if (data && data.error) {
    // Validar que el mensaje no contenga información sensible
    const errorMessage = data.error;
    
    // Si el mensaje contiene información técnica, reemplazarlo
    if (errorMessage.includes('SQL') || 
        errorMessage.includes('Exception') ||
        errorMessage.includes('Constraint') ||
        errorMessage.includes('Table') ||
        errorMessage.includes('Column') ||
        errorMessage.includes('Foreign key')) {
      return 'Ocurrió un error al procesar la solicitud. Por favor, intenta nuevamente.';
    }
    
    return errorMessage;
  }

  // Mensajes según el código de estado HTTP
  switch (response.status) {
    case 400:
      return 'Los datos proporcionados no son válidos. Por favor, verifica los campos e intenta nuevamente.';
    case 401:
      return 'Tu sesión ha expirado o las credenciales son inválidas. Por favor, inicia sesión nuevamente.';
    case 403:
      return 'No tienes permisos para realizar esta acción.';
    case 404:
      return 'El recurso solicitado no fue encontrado.';
    case 409:
      return 'Ya existe un recurso con estos datos. Por favor, verifica la información.';
    case 422:
      return 'Los datos proporcionados no son válidos. Por favor, verifica los campos e intenta nuevamente.';
    case 500:
    case 502:
    case 503:
      return 'Ocurrió un error en el servidor. Por favor, intenta nuevamente más tarde.';
    default:
      return 'Ocurrió un error inesperado. Por favor, intenta nuevamente.';
  }
}

/**
 * Maneja errores de red o de respuesta HTTP
 * @param {Error} error - El error capturado
 * @returns {string} - Mensaje de error seguro para el usuario
 */
export function getNetworkErrorMessage(error) {
  // Errores de red
  if (error instanceof TypeError && error.message.includes('fetch')) {
    return 'No se pudo conectar con el servidor. Por favor, verifica tu conexión a internet.';
  }

  // Timeout
  if (error.name === 'TimeoutError' || error.message.includes('timeout')) {
    return 'La solicitud tardó demasiado tiempo. Por favor, intenta nuevamente.';
  }

  // Error genérico
  return 'Ocurrió un error de red. Por favor, intenta nuevamente.';
}

/**
 * Registra el error completo en la consola (solo para desarrollo)
 * En producción, esto debería enviarse a un servicio de logging
 * @param {Error} error - El error a registrar
 * @param {string} context - Contexto donde ocurrió el error
 */
export function logError(error, context = '') {
  if (process.env.NODE_ENV === 'development') {
    console.error(`[${context}] Error:`, error);
  }
  // En producción, aquí se enviaría a un servicio de logging
  // Ejemplo: sendToLoggingService(error, context);
}

