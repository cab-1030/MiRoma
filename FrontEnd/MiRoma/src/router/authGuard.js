/**
 * Guard de autenticación para Vue Router
 * Protege rutas que requieren autenticación
 */

import { isAuthenticated } from '../utils/auth'

/**
 * Guard que verifica si el usuario está autenticado
 * Si no está autenticado, redirige al login
 * @param {Object} to - Ruta destino
 * @param {Object} from - Ruta origen
 * @param {Function} next - Función para continuar la navegación
 */
export function requireAuth(to, from, next) {
  if (isAuthenticated()) {
    // Usuario autenticado, permitir acceso
    next()
  } else {
    // Usuario no autenticado, redirigir al login
    next({
      name: 'Login',
      query: { redirect: to.fullPath } // Guardar la ruta destino para redirigir después del login
    })
  }
}

/**
 * Guard que redirige al dashboard si el usuario ya está autenticado
 * Útil para rutas públicas como login y register
 * @param {Object} to - Ruta destino
 * @param {Object} from - Ruta origen
 * @param {Function} next - Función para continuar la navegación
 */
export function redirectIfAuthenticated(to, from, next) {
  if (isAuthenticated()) {
    // Usuario ya autenticado, redirigir al dashboard
    next({ name: 'Dashboard' })
  } else {
    // Usuario no autenticado, permitir acceso
    next()
  }
}

