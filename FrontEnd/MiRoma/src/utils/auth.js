/**
 * Utilidades para manejo de autenticación JWT
 */
import { API_ENDPOINTS } from '../config/api'

/**
 * Obtiene el token JWT almacenado en localStorage
 * @returns {string|null} El token JWT o null si no existe
 */
export function getAuthToken() {
  return localStorage.getItem('authToken')
}

/**
 * Guarda el token JWT en localStorage
 * @param {string} token - El token JWT a guardar
 */
export function setAuthToken(token) {
  localStorage.setItem('authToken', token)
}

/**
 * Obtiene el refresh token almacenado en localStorage
 * @returns {string|null} El refresh token o null si no existe
 */
export function getRefreshToken() {
  return localStorage.getItem('refreshToken')
}

/**
 * Guarda el refresh token en localStorage
 * @param {string} token - El refresh token a guardar
 */
export function setRefreshToken(token) {
  localStorage.setItem('refreshToken', token)
}

/**
 * Elimina el token JWT de localStorage
 */
export function removeAuthToken() {
  localStorage.removeItem('authToken')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('userData')
}

/**
 * Verifica si el usuario está autenticado
 * @returns {boolean} true si existe un token, false en caso contrario
 */
export function isAuthenticated() {
  return getAuthToken() !== null
}

/**
 * Obtiene los datos del usuario almacenados
 * @returns {Object|null} Los datos del usuario o null si no existen
 */
export function getUserData() {
  const userData = localStorage.getItem('userData')
  return userData ? JSON.parse(userData) : null
}

/**
 * Obtiene el header Authorization con el token JWT
 * @returns {Object} Objeto con el header Authorization
 */
export function getAuthHeader() {
  const token = getAuthToken()
  return token ? { 'Authorization': `Bearer ${token}` } : {}
}

/**
 * Refresca el access token usando el refresh token
 * @returns {Promise<boolean>} true si se refrescó exitosamente, false en caso contrario
 */
export async function refreshAccessToken() {
  const refreshToken = getRefreshToken()
  
  if (!refreshToken) {
    console.warn('No hay refresh token disponible')
    return false
  }

  try {
    const response = await fetch(`${API_ENDPOINTS.AUTH}/refresh`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ refreshToken })
    })

    if (!response.ok) {
      const error = await response.json()
      console.error('Error al refrescar token:', error)
      return false
    }

    const data = await response.json()
    
    // Guardar los nuevos tokens
    if (data.token) {
      setAuthToken(data.token)
    }
    if (data.refreshToken) {
      setRefreshToken(data.refreshToken)
    }

    return true
  } catch (error) {
    console.error('Error al refrescar token:', error)
    return false
  }
}

/**
 * Verifica si el token está expirado (básico, sin decodificar JWT)
 * Nota: Esta es una verificación básica. Para una verificación real, necesitarías decodificar el JWT
 * @returns {boolean} true si el token podría estar expirado
 */
export function isTokenExpired() {
  // Por ahora, asumimos que el token es válido si existe
  // En producción, deberías decodificar el JWT y verificar la fecha de expiración
  return !getAuthToken()
}

/**
 * Realiza una petición fetch con autenticación JWT
 * Si el token está expirado, intenta refrescarlo automáticamente
 * @param {string} url - La URL de la API
 * @param {Object} options - Opciones adicionales para fetch
 * @param {boolean} options.retryOn401 - Si es true, intenta refrescar el token si recibe 401 (default: true)
 * @returns {Promise<Response>} La respuesta de la petición
 */
export async function authenticatedFetch(url, options = {}) {
  const { retryOn401 = true, ...fetchOptions } = options
  const token = getAuthToken()
  
  const headers = {
    'Content-Type': 'application/json',
    ...fetchOptions.headers
  }
  
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }
  
  let response = await fetch(url, {
    ...fetchOptions,
    headers
  })

  // Si recibimos 401 y tenemos refresh token, intentar refrescar
  if (response.status === 401 && retryOn401 && getRefreshToken()) {
    console.log('Token expirado, intentando refrescar...')
    const refreshed = await refreshAccessToken()
    
    if (refreshed) {
      // Reintentar la petición con el nuevo token
      const newToken = getAuthToken()
      if (newToken) {
        headers['Authorization'] = `Bearer ${newToken}`
        response = await fetch(url, {
          ...fetchOptions,
          headers
        })
      }
    } else {
      // Si no se pudo refrescar, limpiar tokens y redirigir al login
      removeAuthToken()
      // Nota: La redirección al login debería manejarse en el componente que llama a esta función
    }
  }
  
  return response
}

/**
 * Realiza logout del usuario
 * Elimina el token y los datos del usuario del localStorage
 * Opcionalmente notifica al servidor
 * @param {Object} options - Opciones para el logout
 * @param {boolean} options.notifyServer - Si es true, notifica al servidor (default: true)
 * @param {Function} options.onSuccess - Callback ejecutado después del logout exitoso
 * @param {Function} options.onError - Callback ejecutado si hay un error
 * @returns {Promise<void>}
 */
export async function logout(options = {}) {
  const {
    notifyServer = true,
    onSuccess = null,
    onError = null
  } = options

  try {
    // Notificar al servidor (opcional)
    if (notifyServer) {
      const API_BASE_URL = API_ENDPOINTS.AUTH
      const refreshToken = getRefreshToken()
      try {
        await fetch(`${API_BASE_URL}/logout`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ refreshToken: refreshToken || null })
        })
      } catch (error) {
        // Si falla la notificación al servidor, continuamos con el logout local
        console.warn('No se pudo notificar al servidor del logout:', error)
      }
    }

    // Eliminar token y datos del usuario del localStorage
    removeAuthToken()
    localStorage.removeItem('rememberedEmail')

    // Ejecutar callback de éxito si existe
    if (onSuccess) {
      onSuccess()
    }
  } catch (error) {
    console.error('Error durante el logout:', error)
    
    // Aún así, eliminamos el token localmente
    removeAuthToken()
    localStorage.removeItem('rememberedEmail')

    // Ejecutar callback de error si existe
    if (onError) {
      onError(error)
    } else {
      throw error
    }
  }
}

