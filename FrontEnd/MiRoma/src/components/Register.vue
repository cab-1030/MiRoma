<template>
  <div class="register-container">
    <div class="register-card">
      <div class="register-header">
        <h1 class="app-title">Mi Roma</h1>
        <p class="app-subtitle">Crea tu cuenta</p>
      </div>

      <form @submit.prevent="handleRegister" class="register-form">
        <div class="form-row">
          <div class="form-group">
            <label for="nombre">Nombre <span class="required">*</span></label>
            <input
              id="nombre"
              v-model="formData.nombre"
              type="text"
              placeholder="Tu nombre"
              required
              class="form-input"
            />
          </div>

          <div class="form-group">
            <label for="apellido">Apellido <span class="required">*</span></label>
            <input
              id="apellido"
              v-model="formData.apellido"
              type="text"
              placeholder="Tu apellido"
              required
              class="form-input"
            />
          </div>
        </div>

        <div class="form-group">
          <label for="email">Correo Electrónico <span class="required">*</span></label>
          <input
            id="email"
            v-model="formData.email"
            type="email"
            placeholder="tu@email.com"
            required
            class="form-input"
          />
        </div>

        <div class="form-group">
          <label for="telefono">Teléfono</label>
          <input
            id="telefono"
            v-model="formData.telefono"
            type="tel"
            placeholder="+57 300 123 4567"
            class="form-input"
          />
        </div>

        <div class="form-group">
          <label for="fecha_nacimiento">Fecha de Nacimiento <span class="required">*</span></label>
          <input
            id="fecha_nacimiento"
            v-model="formData.fecha_nacimiento"
            type="date"
            required
            class="form-input"
          />
        </div>

        <div class="form-group">
          <label for="rol">Rol <span class="required">*</span></label>
          <select
            id="rol"
            v-model="formData.rol"
            required
            class="form-input form-select"
          >
            <option value="">Selecciona tu rol</option>
            <option value="esposo">Esposo</option>
            <option value="esposa">Esposa</option>
          </select>
        </div>

        <div class="form-group">
          <label for="usuario_pareja">Usuario de tu Pareja</label>
          <input
            id="usuario_pareja"
            v-model="formData.usuario_pareja"
            type="text"
            placeholder="Email o usuario de tu pareja"
            class="form-input"
          />
        </div>

        <div class="form-group">
          <label for="password">Contraseña <span class="required">*</span></label>
          <div class="password-input-wrapper">
            <input
              id="password"
              v-model="formData.password"
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

        <div class="form-group">
          <label for="confirm_password">Confirmar Contraseña <span class="required">*</span></label>
          <div class="password-input-wrapper">
            <input
              id="confirm_password"
              v-model="formData.confirm_password"
              :type="showConfirmPassword ? 'text' : 'password'"
              placeholder="••••••••"
              required
              class="form-input"
              :class="{ 'error': passwordMismatch && formData.confirm_password }"
            />
            <button
              type="button"
              @click="showConfirmPassword = !showConfirmPassword"
              class="password-toggle"
            >
              {{ showConfirmPassword ? '👁️' : '👁️‍🗨️' }}
            </button>
          </div>
          <span v-if="passwordMismatch && formData.confirm_password" class="error-message">
            Las contraseñas no coinciden
          </span>
        </div>

        <div v-if="errorMessage" class="error-alert">
          {{ errorMessage }}
        </div>

        <button type="submit" class="register-button" :disabled="isLoading || passwordMismatch">
          {{ isLoading ? 'Registrando...' : 'Registrarse' }}
        </button>
      </form>

      <div class="register-footer">
        <p>
          ¿Ya tienes una cuenta?
          <router-link to="/login" class="login-link">Inicia sesión aquí</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { 
  validateAndSanitizeEmail, 
  validateAndSanitizeText, 
  validateAndSanitizePhone, 
  validateAndSanitizeDate,
  validatePassword,
  isInputSafe
} from '../utils/validation'
import { getSafeErrorMessage, getNetworkErrorMessage, logError } from '../utils/errorHandler'

const router = useRouter()

import { API_ENDPOINTS } from '../config/api'

// URL de la API del backend
const API_BASE_URL = API_ENDPOINTS.AUTH

const formData = ref({
  nombre: '',
  apellido: '',
  email: '',
  telefono: '',
  fecha_nacimiento: '',
  rol: '',
  usuario_pareja: '',
  password: '',
  confirm_password: ''
})

const showPassword = ref(false)
const showConfirmPassword = ref(false)
const isLoading = ref(false)
const errorMessage = ref('')

const passwordMismatch = computed(() => {
  return formData.value.password && formData.value.confirm_password && 
         formData.value.password !== formData.value.confirm_password
})

const handleRegister = async () => {
  // Limpiar mensaje de error previo
  errorMessage.value = ''

  // Validar y sanitizar nombre
  const nombreSanitizado = validateAndSanitizeText(formData.value.nombre, 100)
  if (!nombreSanitizado || nombreSanitizado.length < 2) {
    errorMessage.value = 'El nombre debe tener al menos 2 caracteres y no contener caracteres especiales.'
    return
  }

  // Validar y sanitizar apellido
  const apellidoSanitizado = validateAndSanitizeText(formData.value.apellido, 100)
  if (!apellidoSanitizado || apellidoSanitizado.length < 2) {
    errorMessage.value = 'El apellido debe tener al menos 2 caracteres y no contener caracteres especiales.'
    return
  }

  // Validar y sanitizar email
  const emailSanitizado = validateAndSanitizeEmail(formData.value.email)
  if (!emailSanitizado) {
    errorMessage.value = 'Por favor, ingresa un email válido.'
    return
  }

  // Validar y sanitizar teléfono (opcional)
  let telefonoSanitizado = null
  if (formData.value.telefono && formData.value.telefono.trim()) {
    telefonoSanitizado = validateAndSanitizePhone(formData.value.telefono)
  }

  // Validar fecha de nacimiento
  const fechaNacimientoValidada = validateAndSanitizeDate(formData.value.fecha_nacimiento)
  if (!fechaNacimientoValidada) {
    errorMessage.value = 'Por favor, ingresa una fecha de nacimiento válida.'
    return
  }

  // Validar rol
  if (!formData.value.rol || (formData.value.rol !== 'esposo' && formData.value.rol !== 'esposa')) {
    errorMessage.value = 'Por favor, selecciona un rol válido.'
    return
  }

  // Validar usuario pareja (opcional)
  let usuarioParejaSanitizado = null
  if (formData.value.usuario_pareja && formData.value.usuario_pareja.trim()) {
    usuarioParejaSanitizado = validateAndSanitizeEmail(formData.value.usuario_pareja)
    if (!usuarioParejaSanitizado) {
      errorMessage.value = 'El email de tu pareja no es válido.'
      return
    }
  }

  // Validar contraseña
  if (!validatePassword(formData.value.password)) {
    errorMessage.value = 'La contraseña debe tener al menos 6 caracteres.'
    return
  }

  // Validar que no contenga caracteres peligrosos
  if (!isInputSafe(formData.value.password)) {
    errorMessage.value = 'La contraseña contiene caracteres no permitidos.'
    return
  }

  // Validar que las contraseñas coincidan
  if (formData.value.password !== formData.value.confirm_password) {
    errorMessage.value = 'Las contraseñas no coinciden. Por favor, verifica e intenta de nuevo.'
    return
  }

  isLoading.value = true
  
  try {
    // Preparar los datos en el formato que espera el backend (camelCase)
    const requestData = {
      nombre: nombreSanitizado,
      apellido: apellidoSanitizado,
      email: emailSanitizado,
      telefono: telefonoSanitizado,
      fechaNacimiento: fechaNacimientoValidada,
      rol: formData.value.rol,
      usuarioPareja: usuarioParejaSanitizado,
      password: formData.value.password,
      confirmPassword: formData.value.confirm_password
    }

    console.log('Enviando datos al servidor:', requestData)

    // Llamar a la API
    const response = await fetch(`${API_BASE_URL}/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(requestData)
    })

    const data = await response.json()

    if (!response.ok) {
      // Manejar errores del servidor de forma segura
      const safeMessage = await getSafeErrorMessage(response, data)
      throw new Error(safeMessage)
    }

    // Registro exitoso
    console.log('Usuario registrado exitosamente:', data)
    alert(`¡Registro exitoso! Bienvenido ${data.nombre}. Por favor, inicia sesión.`)
    
    // Limpiar el formulario
    formData.value = {
      nombre: '',
      apellido: '',
      email: '',
      telefono: '',
      fecha_nacimiento: '',
      rol: '',
      usuario_pareja: '',
      password: '',
      confirm_password: ''
    }
    
    // Redirigir al login después del registro exitoso
    router.push('/login')
  } catch (error) {
    logError(error, 'Register')
    errorMessage.value = error.message || getNetworkErrorMessage(error)
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.register-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-attachment: fixed;
  padding: 1rem;
  margin: 0;
  position: relative;
  box-sizing: border-box;
  left: 0;
  right: 0;
  overflow-y: auto;
}

.register-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  padding: 2rem;
  width: 100%;
  max-width: 500px;
  margin: 1rem 0;
  animation: slideUp 0.5s ease-out;
  box-sizing: border-box;
  flex-shrink: 0;
  max-height: calc(100vh - 2rem);
  overflow-y: auto;
  overflow-x: hidden;
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

.register-header {
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

.register-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
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

.required {
  color: #ef4444;
}

.form-input {
  padding: 0.875rem 1rem;
  border: 2px solid #e5e7eb;
  border-radius: 10px;
  font-size: 1rem;
  transition: all 0.3s ease;
  outline: none;
  width: 100%;
  font-family: inherit;
}

.form-input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-select {
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%23374151' d='M6 9L1 4h10z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 1rem center;
  padding-right: 2.5rem;
  cursor: pointer;
}

.form-select:focus {
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

.error-message {
  color: #ef4444;
  font-size: 0.75rem;
  margin-top: 0.25rem;
}

.error-alert {
  background-color: #fee2e2;
  border: 1px solid #ef4444;
  color: #991b1b;
  padding: 0.875rem 1rem;
  border-radius: 10px;
  font-size: 0.875rem;
  margin-bottom: 1rem;
  text-align: center;
}

.form-input.error {
  border-color: #ef4444;
}

.form-input.error:focus {
  border-color: #ef4444;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
}

.register-card::-webkit-scrollbar {
  width: 8px;
}

.register-card::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.register-card::-webkit-scrollbar-thumb {
  background: #667eea;
  border-radius: 10px;
}

.register-card::-webkit-scrollbar-thumb:hover {
  background: #764ba2;
}

.register-button {
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

.register-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(102, 126, 234, 0.4);
}

.register-button:active:not(:disabled) {
  transform: translateY(0);
}

.register-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.register-footer {
  margin-top: 1.5rem;
  text-align: center;
  padding-top: 1.5rem;
  border-top: 1px solid #e5e7eb;
}

.register-footer p {
  color: #6b7280;
  font-size: 0.875rem;
  margin: 0;
}

.login-link {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.3s ease;
}

.login-link:hover {
  color: #764ba2;
}

/* Móviles pequeños (hasta 360px) */
@media (max-width: 360px) {
  .register-container {
    padding: 0.75rem;
    display: flex;
    align-items: flex-start;
    justify-content: center;
    left: 0;
    right: 0;
  }

  .register-card {
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

  .register-form {
    gap: 1rem;
  }

  .form-row {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .form-input {
    padding: 0.75rem 0.875rem;
    font-size: 0.9375rem;
  }

  .register-button {
    padding: 0.875rem;
    font-size: 0.9375rem;
  }
}

/* Móviles (361px - 480px) */
@media (min-width: 361px) and (max-width: 480px) {
  .register-container {
    display: flex;
    align-items: flex-start;
    justify-content: center;
    left: 0;
    right: 0;
  }

  .register-card {
    padding: 2rem 1.5rem;
    margin: 0;
  }

  .form-row {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .app-title {
    font-size: 2rem;
  }
}

/* Tablets pequeñas (481px - 768px) */
@media (min-width: 481px) and (max-width: 768px) {
  .register-container {
    padding: 1.5rem;
    display: flex;
    align-items: flex-start;
    justify-content: center;
    left: 0;
    right: 0;
  }

  .register-card {
    padding: 2.5rem;
    max-width: 480px;
    margin: 0;
  }

  .app-title {
    font-size: 2.25rem;
  }

  .app-subtitle {
    font-size: 0.9375rem;
  }

  .register-form {
    gap: 1.5rem;
  }
}

/* Tablets grandes (769px - 1023px) */
@media (min-width: 769px) and (max-width: 1023px) {
  .register-container {
    padding: 2rem;
    display: flex;
    align-items: flex-start;
    justify-content: center;
    left: 0;
    right: 0;
  }

  .register-card {
    padding: 3rem;
    max-width: 500px;
    margin: 0;
  }

  .app-title {
    font-size: 2.5rem;
  }

  .app-subtitle {
    font-size: 1rem;
  }

  .register-header {
    margin-bottom: 2.5rem;
  }

  .register-form {
    gap: 1.5rem;
  }
}

/* Desktop (1024px en adelante) */
@media (min-width: 1024px) {
  .register-container {
    width: 100%;
    padding: 2rem;
    margin: 0;
    display: flex;
    align-items: flex-start;
    justify-content: center;
    left: 0;
    right: 0;
    padding-top: 2rem;
  }

  .register-card {
    padding: 3.5rem;
    max-width: 550px;
    border-radius: 24px;
    margin: 0;
  }

  .app-title {
    font-size: 2.75rem;
  }

  .app-subtitle {
    font-size: 1.0625rem;
  }

  .register-header {
    margin-bottom: 2.5rem;
  }

  .register-form {
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

  .register-button {
    padding: 1.125rem;
    font-size: 1.0625rem;
    border-radius: 12px;
  }

  .register-footer {
    margin-top: 2rem;
    padding-top: 2rem;
  }

  .register-footer p {
    font-size: 0.9375rem;
  }
}

/* Desktop grande (más de 1440px) */
@media (min-width: 1441px) {
  .register-container {
    padding: 3rem;
    display: flex;
    align-items: flex-start;
    justify-content: center;
    left: 0;
    right: 0;
    padding-top: 3rem;
  }

  .register-card {
    padding: 4rem;
    max-width: 600px;
    margin: 0;
  }

  .app-title {
    font-size: 3.25rem;
  }

  .app-subtitle {
    font-size: 1.125rem;
  }

  .register-header {
    margin-bottom: 3rem;
  }

  .form-group label {
    font-size: 1rem;
  }

  .form-input {
    padding: 1.125rem 1.25rem;
    font-size: 1.125rem;
  }

  .register-button {
    padding: 1.25rem;
    font-size: 1.125rem;
  }

  .register-footer p {
    font-size: 1rem;
  }
}

/* Orientación landscape en móviles */
@media (max-height: 600px) and (orientation: landscape) {
  .register-container {
    padding: 1rem;
    display: flex;
    align-items: flex-start;
    justify-content: center;
    left: 0;
    right: 0;
  }

  .register-card {
    max-height: calc(100vh - 2rem);
  }
}

  .register-card {
    padding: 1.5rem;
    margin: 0;
  }

  .register-header {
    margin-bottom: 1.5rem;
  }

  .app-title {
    font-size: 1.75rem;
    margin-bottom: 0.25rem;
  }

  .app-subtitle {
    font-size: 0.8125rem;
  }

  .register-form {
    gap: 1rem;
  }

  .register-footer {
    margin-top: 1rem;
    padding-top: 1rem;
  }
</style>
