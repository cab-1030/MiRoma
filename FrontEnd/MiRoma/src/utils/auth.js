/**
 * Utilidades para manejo de autenticación JWT
 * Los tokens ahora se almacenan en cookies HTTP-only, Secure y SameSite
 */
import { API_ENDPOINTS } from '../config/api'

/**
 * Obtiene el token JWT de las cookies
 * Nota: Como las cookies son HTTP-only, no podemos leerlas desde JavaScript
 * El token se envía automáticamente en las peticiones
 * @returns {string|null} Siempre null, ya que las cookies HTTP-only no son accesibles desde JS
 */
export function getAuthToken() {
  // Las cookies HTTP-only no son accesibles desde JavaScript
  // El navegador las envía automáticamente en las peticiones
  return null
}

/**
 * Guarda el token JWT en cookies (manejado por el servidor)
 * @param {string} token - El token JWT a guardar (ya no se usa, el servidor lo maneja)
 */
export function setAuthToken(token) {
  // Los tokens ahora se establecen en cookies por el servidor
  // No necesitamos hacer nada aquí
}

/**
 * Obtiene el refresh token de las cookies
 * Nota: Como las cookies son HTTP-only, no podemos leerlas desde JavaScript
 * @returns {string|null} Siempre null, ya que las cookies HTTP-only no son accesibles desde JS
 */
export function getRefreshToken() {
  // Las cookies HTTP-only no son accesibles desde JavaScript
  return null
}

/**
 * Guarda el refresh token en cookies (manejado por el servidor)
 * @param {string} token - El refresh token a guardar (ya no se usa, el servidor lo maneja)
 */
export function setRefreshToken(token) {
  // Los tokens ahora se establecen en cookies por el servidor
  // No necesitamos hacer nada aquí
}

/**
 * Elimina los tokens (las cookies se eliminan en el servidor durante logout)
 */
export function removeAuthToken() {
  // Las cookies se eliminan automáticamente cuando el servidor las establece con Max-Age=0
  // Solo eliminamos datos del usuario de localStorage
  localStorage.removeItem('userData')
}

/**
 * Verifica si el usuario está autenticado
 * Como las cookies HTTP-only no son accesibles, verificamos si hay datos de usuario
 * @returns {boolean} true si hay datos de usuario, false en caso contrario
 */
export function isAuthenticated() {
  // Verificar si hay datos de usuario almacenados
  // El token real está en una cookie HTTP-only que el navegador envía automáticamente
  const userData = getUserData()
  return userData !== null && userData.id !== null
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
 * Obtiene los headers para peticiones autenticadas
 * Nota: El token se envía automáticamente en cookies, pero mantenemos el header para compatibilidad
 * @returns {Object} Objeto con headers (incluyendo credentials)
 */
export function getAuthHeader() {
  // Las cookies se envían automáticamente, pero no incluimos el header Authorization
  // ya que el token está en cookies HTTP-only
  return {}
}

/**
 * Refresca el access token usando el refresh token
 * @returns {Promise<boolean>} true si se refrescó exitosamente, false en caso contrario
 */
export async function refreshAccessToken() {
  try {
    // El refresh token se envía automáticamente en la cookie
    const response = await fetch(`${API_ENDPOINTS.AUTH}/refresh`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include', // Incluir cookies en la petición
      body: JSON.stringify({}) // El refresh token viene de la cookie
    })

    if (!response.ok) {
      const error = await response.json()
      console.error('Error al refrescar token:', error)
      return false
    }

    const data = await response.json()
    
    // Los nuevos tokens se establecen automáticamente en cookies por el servidor
    // No necesitamos guardarlos manualmente

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
  
  const headers = {
    'Content-Type': 'application/json',
    ...fetchOptions.headers
  }
  
  // Las cookies se envían automáticamente con credentials: 'include'
  // No necesitamos agregar el header Authorization manualmente
  
  let response = await fetch(url, {
    ...fetchOptions,
    headers,
    credentials: 'include' // Incluir cookies en la petición
  })

  // Si recibimos 401, intentar refrescar el token
  if (response.status === 401 && retryOn401) {
    console.log('Token expirado, intentando refrescar...')
    const refreshed = await refreshAccessToken()
    
    if (refreshed) {
      // Reintentar la petición (las cookies se envían automáticamente)
      response = await fetch(url, {
        ...fetchOptions,
        headers,
        credentials: 'include'
      })
    } else {
      // Si no se pudo refrescar, limpiar datos y redirigir al login
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
      try {
        await fetch(`${API_BASE_URL}/logout`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          credentials: 'include', // Incluir cookies para que el servidor pueda invalidar el token
          body: JSON.stringify({})
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

