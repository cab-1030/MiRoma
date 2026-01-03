<template>
  <button 
    @click="handleLogout" 
    :disabled="isLoggingOut"
    class="logout-button"
  >
    {{ isLoggingOut ? 'Cerrando sesión...' : 'Cerrar Sesión' }}
  </button>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { logout } from '../utils/auth'

const router = useRouter()
const isLoggingOut = ref(false)

const handleLogout = async () => {
  isLoggingOut.value = true
  
  try {
    await logout({
      notifyServer: true,
      onSuccess: () => {
        // Redirigir al login después del logout exitoso
        router.push({ name: 'Login' })
      },
      onError: (error) => {
        console.error('Error durante el logout:', error)
        // Aún así redirigir al login
        router.push({ name: 'Login' })
      }
    })
  } catch (error) {
    console.error('Error durante el logout:', error)
    // Aún así redirigir al login
    router.push({ name: 'Login' })
  } finally {
    isLoggingOut.value = false
  }
}
</script>

<style scoped>
.logout-button {
  padding: 0.75rem 1.5rem;
  background-color: #ef4444;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.logout-button:hover:not(:disabled) {
  background-color: #dc2626;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.4);
}

.logout-button:active:not(:disabled) {
  transform: translateY(0);
}

.logout-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>

