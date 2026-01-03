<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1 class="app-title">Mi Roma</h1>
        <p class="app-subtitle">Administración Financiera para Parejas</p>
      </div>

      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label for="email">Correo Electrónico</label>
          <input
            id="email"
            v-model="email"
            type="email"
            placeholder="tu@email.com"
            required
            class="form-input"
          />
        </div>

        <div class="form-group">
          <label for="password">Contraseña</label>
          <div class="password-input-wrapper">
            <input
              id="password"
              v-model="password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="••••••••"
              required
              class="form-input"
            />
            <button
              type="button"
              @click="showPassword = !showPassword"
              class="password-toggle"
            >
              {{ showPassword ? '👁️' : '👁️‍🗨️' }}
            </button>
          </div>
        </div>

        <div class="form-options">
          <label class="remember-me">
            <input type="checkbox" v-model="rememberMe" />
            <span>Recordarme</span>
          </label>
          <a href="#" class="forgot-password">¿Olvidaste tu contraseña?</a>
        </div>

        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>

        <button type="submit" class="login-button" :disabled="isLoading">
          {{ isLoading ? 'Iniciando sesión...' : 'Iniciar Sesión' }}
        </button>
      </form>

      <div class="login-footer">
        <p>
          ¿No tienes una cuenta?
          <router-link to="/register" class="signup-link">Regístrate aquí</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { validateAndSanitizeEmail, validatePassword, isInputSafe } from '../utils/validation'
import { getSafeErrorMessage, getNetworkErrorMessage, logError } from '../utils/errorHandler'

const router = useRouter()

import { API_ENDPOINTS } from '../config/api'

// URL de la API del backend
const API_BASE_URL = API_ENDPOINTS.AUTH

const email = ref('')
const password = ref('')
const showPassword = ref(false)
const rememberMe = ref(false)
const isLoading = ref(false)
const errorMessage = ref('')

const handleLogin = async () => {
  // Limpiar mensaje de error previo
  errorMessage.value = ''
  
  // Validación básica
  if (!email.value || !password.value) {
    errorMessage.value = 'Por favor, completa todos los campos.'
    return
  }

  // Validar y sanitizar email
  const sanitizedEmail = validateAndSanitizeEmail(email.value)
  if (!sanitizedEmail) {
    errorMessage.value = 'Por favor, ingresa un email válido.'
    return
  }

  // Validar contraseña
  if (!validatePassword(password.value)) {
    errorMessage.value = 'La contraseña debe tener al menos 6 caracteres.'
    return
  }

  // Validar que no contenga caracteres peligrosos
  if (!isInputSafe(password.value)) {
    errorMessage.value = 'La contraseña contiene caracteres no permitidos.'
    return
  }

  isLoading.value = true
  
  try {
    // Preparar los datos para el login
    const requestData = {
      email: sanitizedEmail,
      password: password.value
    }

    // Llamar a la API de login
    const response = await fetch(`${API_BASE_URL}/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(requestData)
    })

    const data = await response.json()

    if (!response.ok) {
      // Manejar errores del servidor de forma segura
      errorMessage.value = await getSafeErrorMessage(response, data)
      logError(new Error(`Login failed: ${response.status}`), 'Login')
      return
    }

    // Login exitoso - guardar el token
    if (data.token) {
      // Guardar el access token en localStorage
      localStorage.setItem('authToken', data.token)
      
      // Guardar el refresh token si está disponible
      if (data.refreshToken) {
        localStorage.setItem('refreshToken', data.refreshToken)
      }
      
      // Guardar datos del usuario
      localStorage.setItem('userData', JSON.stringify({
        id: data.id,
        nombre: data.nombre,
        email: data.email,
        parejaId: data.parejaId,
        hasPartner: data.hasPartner
      }))
      
      // Si el usuario marcó "Recordarme", guardar también el email
      if (rememberMe.value) {
        localStorage.setItem('rememberedEmail', email.value)
      } else {
        localStorage.removeItem('rememberedEmail')
      }

      console.log('Login exitoso. Tokens guardados.')
      
      // Redirigir según si tiene pareja o no
      const redirectPath = router.currentRoute.value.query.redirect
      
      // Si no tiene pareja vinculada, redirigir al formulario de vinculación
      if (!data.hasPartner) {
        router.push('/link-partner')
      } else {
        // Si tiene pareja, ir al dashboard o a la ruta de redirección
        router.push(redirectPath || '/dashboard')
      }
    } else {
      errorMessage.value = 'Error: No se recibió el token de autenticación.'
    }
  } catch (error) {
    logError(error, 'Login')
    errorMessage.value = getNetworkErrorMessage(error)
  } finally {
    isLoading.value = false
  }
}

// Cargar email recordado si existe
const rememberedEmail = localStorage.getItem('rememberedEmail')
if (rememberedEmail) {
  email.value = rememberedEmail
  rememberMe.value = true
}
</script>

<style scoped>
.login-container {
  width: 100%;
  min-height: 100vh;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-attachment: fixed;
  padding: 1rem;
  margin: 0;
  position: relative;
  box-sizing: border-box;
  left: 0;
  right: 0;
}

.login-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  padding: 2rem;
  width: 100%;
  max-width: 450px;
  margin: 0;
  animation: slideUp 0.5s ease-out;
  box-sizing: border-box;
  flex-shrink: 0;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 2rem;
}

.app-title {
  font-size: 2rem;
  font-weight: 700;
  color: #667eea;
  margin: 0 0 0.5rem 0;
  letter-spacing: -0.5px;
}

.app-subtitle {
  color: #6b7280;
  font-size: 0.875rem;
  margin: 0;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
}

.form-input {
  padding: 0.875rem 1rem;
  border: 2px solid #e5e7eb;
  border-radius: 10px;
  font-size: 1rem;
  transition: all 0.3s ease;
  outline: none;
  width: 100%;
}

.form-input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.password-input-wrapper {
  position: relative;
}

.password-toggle {
  position: absolute;
  right: 1rem;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1.2rem;
  padding: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.875rem;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  color: #6b7280;
}

.remember-me input[type="checkbox"] {
  cursor: pointer;
  width: 1rem;
  height: 1rem;
}

.forgot-password {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
  white-space: nowrap;
}

.forgot-password:hover {
  color: #764ba2;
}

.login-button {
  padding: 1rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 0.5rem;
  width: 100%;
}

.login-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(102, 126, 234, 0.4);
}

.login-button:active:not(:disabled) {
  transform: translateY(0);
}

.login-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.error-message {
  padding: 0.75rem 1rem;
  background-color: #fee2e2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  color: #dc2626;
  font-size: 0.875rem;
  margin-top: 0.5rem;
  text-align: center;
}

.login-footer {
  margin-top: 1.5rem;
  text-align: center;
  padding-top: 1.5rem;
  border-top: 1px solid #e5e7eb;
}

.login-footer p {
  color: #6b7280;
  font-size: 0.875rem;
  margin: 0;
}

.signup-link {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.3s ease;
}

.signup-link:hover {
  color: #764ba2;
}

/* Móviles pequeños (hasta 360px) */
@media (max-width: 360px) {
  .login-container {
    padding: 0.75rem;
    display: flex;
    align-items: center;
    justify-content: center;
    left: 0;
    right: 0;
  }

  .login-card {
    padding: 1.5rem 1.25rem;
    border-radius: 16px;
    margin: 0;
  }

  .app-title {
    font-size: 1.75rem;
  }

  .app-subtitle {
    font-size: 0.8125rem;
  }

  .login-form {
    gap: 1rem;
  }

  .form-input {
    padding: 0.75rem 0.875rem;
    font-size: 0.9375rem;
  }

  .login-button {
    padding: 0.875rem;
    font-size: 0.9375rem;
  }
}

/* Móviles (361px - 480px) */
@media (min-width: 361px) and (max-width: 480px) {
  .login-container {
    display: flex;
    align-items: center;
    justify-content: center;
    left: 0;
    right: 0;
  }

  .login-card {
    padding: 2rem 1.5rem;
    margin: 0;
  }

  .app-title {
    font-size: 2rem;
  }
}

/* Tablets pequeñas (481px - 768px) */
@media (min-width: 481px) and (max-width: 768px) {
  .login-container {
    padding: 1.5rem;
    display: flex;
    align-items: center;
    justify-content: center;
    left: 0;
    right: 0;
  }

  .login-card {
    padding: 2.5rem;
    max-width: 420px;
    margin: 0;
  }

  .app-title {
    font-size: 2.25rem;
  }

  .app-subtitle {
    font-size: 0.9375rem;
  }

  .login-form {
    gap: 1.5rem;
  }
}

/* Tablets grandes (769px - 1023px) */
@media (min-width: 769px) and (max-width: 1023px) {
  .login-container {
    padding: 2rem;
    display: flex;
    align-items: center;
    justify-content: center;
    left: 0;
    right: 0;
  }

  .login-card {
    padding: 3rem;
    max-width: 450px;
    margin: 0;
  }

  .app-title {
    font-size: 2.5rem;
  }

  .app-subtitle {
    font-size: 1rem;
  }

  .login-header {
    margin-bottom: 2.5rem;
  }

  .login-form {
    gap: 1.5rem;
  }
}

/* Desktop (1024px en adelante) */
@media (min-width: 1024px) {
  .login-container {
    width: 100%;
    padding: 2rem;
    margin: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    left: 0;
    right: 0;
  }

  .login-card {
    padding: 3.5rem;
    max-width: 500px;
    border-radius: 24px;
    margin: 0;
  }

  .app-title {
    font-size: 2.75rem;
  }

  .app-subtitle {
    font-size: 1.0625rem;
  }

  .login-header {
    margin-bottom: 2.5rem;
  }

  .login-form {
    gap: 1.75rem;
  }

  .form-group label {
    font-size: 0.9375rem;
  }

  .form-input {
    padding: 1rem 1.125rem;
    font-size: 1.0625rem;
    border-radius: 12px;
  }

  .login-button {
    padding: 1.125rem;
    font-size: 1.0625rem;
    border-radius: 12px;
  }

  .form-options {
    font-size: 0.9375rem;
  }

  .login-footer {
    margin-top: 2rem;
    padding-top: 2rem;
  }

  .login-footer p {
    font-size: 0.9375rem;
  }
}

/* Desktop grande (más de 1440px) */
@media (min-width: 1441px) {
  .login-container {
    padding: 3rem;
    display: flex;
    align-items: center;
    justify-content: center;
    left: 0;
    right: 0;
  }

  .login-card {
    padding: 4rem;
    max-width: 550px;
    margin: 0;
  }

  .app-title {
    font-size: 3.25rem;
  }

  .app-subtitle {
    font-size: 1.125rem;
  }

  .login-header {
    margin-bottom: 3rem;
  }

  .form-group label {
    font-size: 1rem;
  }

  .form-input {
    padding: 1.125rem 1.25rem;
    font-size: 1.125rem;
  }

  .login-button {
    padding: 1.25rem;
    font-size: 1.125rem;
  }

  .form-options {
    font-size: 1rem;
  }

  .login-footer p {
    font-size: 1rem;
  }
}

/* Orientación landscape en móviles */
@media (max-height: 600px) and (orientation: landscape) {
  .login-container {
    padding: 1rem;
    display: flex;
    align-items: center;
    justify-content: center;
    left: 0;
    right: 0;
  }

  .login-card {
    padding: 1.5rem;
    margin: 0;
  }

  .login-header {
    margin-bottom: 1.5rem;
  }

  .app-title {
    font-size: 1.75rem;
    margin-bottom: 0.25rem;
  }

  .app-subtitle {
    font-size: 0.8125rem;
  }

  .login-form {
    gap: 1rem;
  }

  .login-footer {
    margin-top: 1rem;
    padding-top: 1rem;
  }
}
</style>
