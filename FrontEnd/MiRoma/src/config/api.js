/**
 * Configuración centralizada de URLs de API
 * Permite cambiar fácilmente entre entornos (desarrollo, producción)
 */

// Determinar el entorno
const isDevelopment = import.meta.env.DEV || import.meta.env.MODE === 'development'
const isProduction = import.meta.env.PROD || import.meta.env.MODE === 'production'

// Configuración de URLs base
const API_CONFIG = {
  // URL base del backend
  // En desarrollo, usar el proxy de Vite (relativo) que maneja HTTPS
  // En producción, usar HTTPS directo
  BASE_URL: import.meta.env.VITE_API_BASE_URL || 
    (isDevelopment 
      ? '/api' // Usar proxy relativo en desarrollo (Vite maneja HTTPS al backend)
      : 'https://api.miroma.com/api'),
  
  // Protocolo (siempre HTTPS en producción)
  PROTOCOL: 'https',
  
  // Puerto por defecto
  PORT: isDevelopment ? 5173 : 443,
}

// Construir URL base completa
let API_BASE_URL
if (isDevelopment && API_CONFIG.BASE_URL.startsWith('/')) {
  // En desarrollo con proxy, usar ruta relativa (el proxy maneja HTTPS)
  API_BASE_URL = ''
} else {
  // En producción o URL completa especificada
  API_BASE_URL = API_CONFIG.BASE_URL.replace(/^https?:\/\//, '')
  if (!API_BASE_URL.startsWith('/')) {
    API_BASE_URL = `${API_CONFIG.PROTOCOL}://${API_BASE_URL}`
  }
}

// Endpoints específicos
export const API_ENDPOINTS = {
  AUTH: `${API_BASE_URL}/api/auth`,
  INGRESOS: `${API_BASE_URL}/api/ingresos`,
  EGRESOS: `${API_BASE_URL}/api/egresos`,
  CATEGORIAS: `${API_BASE_URL}/api/categorias-egresos`,
  PRESUPUESTOS: `${API_BASE_URL}/api/presupuestos`,
  PRESUPUESTOS_CATEGORIAS: `${API_BASE_URL}/api/presupuestos-categorias`,
  RESUMEN_FINANCIERO: `${API_BASE_URL}/api/resumen-financiero`,
  LOG_EVENTOS: `${API_BASE_URL}/api/log-eventos`,
}

// Exportar configuración completa
export default {
  ...API_CONFIG,
  ENDPOINTS: API_ENDPOINTS,
}
