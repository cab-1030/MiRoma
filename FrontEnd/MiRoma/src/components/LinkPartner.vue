<template>
  <div class="link-partner-container">
    <div class="link-partner-card">
      <div class="link-partner-header">
        <h1 class="title">Vincular Pareja</h1>
        <p class="subtitle">Ingresa el email de tu pareja para vincular tu cuenta</p>
      </div>

      <form @submit.prevent="handleCheckPartner" class="link-partner-form">
        <div class="form-group">
          <label for="partnerEmail">Email de tu Pareja <span class="required">*</span></label>
          <input
            id="partnerEmail"
            v-model="partnerEmail"
            type="email"
            placeholder="pareja@email.com"
            required
            class="form-input"
            :disabled="isChecking || isLinking"
          />
        </div>

        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>

        <div v-if="userFound && !isLinking" class="success-message">
          <p>✓ Usuario encontrado: <strong>{{ partnerInfo.nombre }}</strong></p>
          <p class="email-info">{{ partnerInfo.email }}</p>
        </div>

        <div class="button-group">
          <button 
            v-if="!userFound"
            type="submit" 
            class="primary-button" 
            :disabled="isChecking || isLinking"
          >
            {{ isChecking ? 'Verificando...' : 'Verificar Usuario' }}
          </button>

          <button 
            v-if="userFound && !isLinking"
            type="button"
            @click="handleLinkPartner"
            class="primary-button"
            :disabled="isLinking"
          >
            Vincular Pareja
          </button>

          <button 
            v-if="isLinking"
            type="button"
            class="primary-button"
            disabled
          >
            Vinculando...
          </button>
        </div>
      </form>

      <div v-if="!userFound && !isChecking" class="help-section">
        <p class="help-text">
          ¿Tu pareja aún no tiene cuenta?
        </p>
        <router-link to="/register" class="register-link">
          Invita a tu pareja a registrarse
        </router-link>
      </div>

      <div v-if="userFound && !isLinking" class="skip-section">
        <button 
          type="button"
          @click="skipLinking"
          class="skip-button"
        >
          Omitir por ahora
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authenticatedFetch, getUserData } from '../utils/auth'
import { validateAndSanitizeEmail } from '../utils/validation'

const router = useRouter()

import { API_ENDPOINTS } from '../config/api'

// URL de la API del backend
const API_BASE_URL = API_ENDPOINTS.AUTH

const partnerEmail = ref('')
const isChecking = ref(false)
const isLinking = ref(false)
const errorMessage = ref('')
const userFound = ref(false)
const partnerInfo = ref(null)

const handleCheckPartner = async () => {
  errorMessage.value = ''
  userFound.value = false
  partnerInfo.value = null

  // Validar y sanitizar email
  const emailSanitizado = validateAndSanitizeEmail(partnerEmail.value)
  if (!emailSanitizado) {
    errorMessage.value = 'Por favor, ingresa un email válido.'
    return
  }

  isChecking.value = true

  try {
    const response = await fetch(`${API_BASE_URL}/check-user`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ email: emailSanitizado })
    })

    const data = await response.json()

    if (!response.ok) {
      errorMessage.value = data.error || 'Error al verificar el usuario.'
      return
    }

    if (data.exists) {
      userFound.value = true
      partnerInfo.value = {
        nombre: data.nombre,
        email: data.email,
        userId: data.userId
      }
      errorMessage.value = ''
    } else {
      errorMessage.value = 'El usuario no existe en la plataforma. Invita a tu pareja a registrarse.'
      userFound.value = false
    }
  } catch (error) {
    console.error('Error al verificar usuario:', error)
    errorMessage.value = 'Error de red o del servidor. Intenta de nuevo más tarde.'
  } finally {
    isChecking.value = false
  }
}

const handleLinkPartner = async () => {
  if (!userFound.value || !partnerInfo.value) {
    errorMessage.value = 'Por favor, verifica primero el usuario de tu pareja.'
    return
  }

  // Validar y sanitizar email nuevamente
  const emailSanitizado = validateAndSanitizeEmail(partnerEmail.value)
  if (!emailSanitizado) {
    errorMessage.value = 'Por favor, ingresa un email válido.'
    return
  }

  isLinking.value = true
  errorMessage.value = ''

  try {
    console.log('Intentando vincular pareja:', emailSanitizado)
    
    const response = await authenticatedFetch(`${API_BASE_URL}/link-partner`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ partnerEmail: emailSanitizado })
    })

    console.log('Respuesta del servidor:', response.status, response.statusText)

    // Verificar si la respuesta es JSON antes de parsear
    const contentType = response.headers.get('content-type')
    let data
    
    if (contentType && contentType.includes('application/json')) {
      data = await response.json()
    } else {
      const text = await response.text()
      console.error('Respuesta no es JSON:', text)
      errorMessage.value = `Error del servidor: ${response.status} ${response.statusText}`
      return
    }

    if (!response.ok) {
      console.error('Error del servidor:', data)
      errorMessage.value = data.error || `Error al vincular la pareja. (${response.status})`
      return
    }

    // Pareja vinculada exitosamente
    alert(`¡Pareja vinculada exitosamente con ${data.partnerNombre}!`)
    router.push('/dashboard')
  } catch (error) {
    console.error('Error al vincular pareja:', error)
    if (error.message) {
      errorMessage.value = `Error: ${error.message}`
    } else {
      errorMessage.value = 'Error de red o del servidor. Intenta de nuevo más tarde.'
    }
  } finally {
    isLinking.value = false
  }
}

const skipLinking = () => {
  router.push('/dashboard')
}
</script>

<style scoped>
.link-partner-container {
  width: 100%;
  min-height: 100vh;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-attachment: fixed;
  padding: 2rem;
  margin: 0;
  position: relative;
  box-sizing: border-box;
}

.link-partner-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  padding: 2.5rem;
  width: 100%;
  max-width: 500px;
  margin: 0;
  animation: slideUp 0.5s ease-out;
  box-sizing: border-box;
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

.link-partner-header {
  text-align: center;
  margin-bottom: 2rem;
}

.title {
  font-size: 2rem;
  font-weight: 700;
  color: #667eea;
  margin: 0 0 0.5rem 0;
  letter-spacing: -0.5px;
}

.subtitle {
  color: #6b7280;
  font-size: 0.875rem;
  margin: 0;
}

.link-partner-form {
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

.required {
  color: #ef4444;
}

.form-input {
  padding: 0.875rem 1rem;
  border: 2px solid #e5e7eb;
  border-radius: 10px;
  font-size: 1rem;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-input:disabled {
  background-color: #f3f4f6;
  cursor: not-allowed;
}

.error-message {
  padding: 0.75rem 1rem;
  background-color: #fee2e2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  color: #dc2626;
  font-size: 0.875rem;
  text-align: center;
}

.success-message {
  padding: 1rem;
  background-color: #d1fae5;
  border: 1px solid #a7f3d0;
  border-radius: 8px;
  color: #065f46;
  font-size: 0.875rem;
}

.success-message p {
  margin: 0.25rem 0;
}

.email-info {
  font-size: 0.75rem;
  color: #047857;
}

.button-group {
  display: flex;
  gap: 1rem;
  margin-top: 0.5rem;
}

.primary-button {
  flex: 1;
  padding: 1rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.primary-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(102, 126, 234, 0.4);
}

.primary-button:active:not(:disabled) {
  transform: translateY(0);
}

.primary-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.help-section {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid #e5e7eb;
  text-align: center;
}

.help-text {
  color: #6b7280;
  font-size: 0.875rem;
  margin: 0 0 0.75rem 0;
}

.register-link {
  display: inline-block;
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  font-size: 0.875rem;
  transition: color 0.3s ease;
}

.register-link:hover {
  color: #764ba2;
  text-decoration: underline;
}

.skip-section {
  margin-top: 1rem;
  text-align: center;
}

.skip-button {
  background: none;
  border: none;
  color: #6b7280;
  font-size: 0.875rem;
  cursor: pointer;
  text-decoration: underline;
  transition: color 0.3s ease;
}

.skip-button:hover {
  color: #374151;
}

@media (max-width: 768px) {
  .link-partner-container {
    padding: 1rem;
  }

  .link-partner-card {
    padding: 1.5rem;
  }

  .title {
    font-size: 1.5rem;
  }
}
</style>

