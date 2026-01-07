<template>
  <div class="comportamientos-extranos-container">
    <div class="header-section">
      <h2>Comportamientos Extraños</h2>
      <p class="subtitle">Actividades sospechosas detectadas en el sistema</p>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>Cargando comportamientos extraños...</p>
    </div>

    <div v-else-if="error" class="error-state">
      <div class="error-icon">⚠️</div>
      <p class="error-message">{{ error }}</p>
      <button @click="cargarComportamientos" class="retry-button">Reintentar</button>
    </div>

    <div v-else-if="comportamientos.length === 0" class="empty-state">
      <div class="empty-icon">✅</div>
      <p>No se han detectado comportamientos extraños en el sistema.</p>
    </div>

    <div v-else class="comportamientos-list">
      <div 
        v-for="comportamiento in comportamientos" 
        :key="comportamiento.id"
        class="comportamiento-card"
      >
        <div class="card-header">
          <div class="card-icon">🚨</div>
          <div class="card-title-section">
            <h3 class="card-title">{{ comportamiento.tipoComportamiento }}</h3>
            <p class="card-email">{{ comportamiento.email }}</p>
          </div>
        </div>
        
        <div class="card-body">
          <p class="card-description">{{ comportamiento.descripcion }}</p>
          
          <div class="card-details">
            <div class="detail-item">
              <span class="detail-label">Intentos fallidos:</span>
              <span class="detail-value highlight">{{ comportamiento.intentosFallidos }}</span>
            </div>
            
            <div class="detail-item">
              <span class="detail-label">Nivel de bloqueo:</span>
              <span class="detail-value">{{ comportamiento.nivelBloqueo }}</span>
            </div>
            
            <div class="detail-item">
              <span class="detail-label">Último intento:</span>
              <span class="detail-value">{{ formatearFecha(comportamiento.ultimoIntento) }}</span>
            </div>
            
            <div v-if="comportamiento.bloqueadoHasta" class="detail-item">
              <span class="detail-label">Bloqueado hasta:</span>
              <span class="detail-value warning">{{ formatearFecha(comportamiento.bloqueadoHasta) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { authenticatedFetch } from '../utils/auth'
import { getSafeErrorMessage, getNetworkErrorMessage, logError } from '../utils/errorHandler'
import { API_ENDPOINTS } from '../config/api'

const comportamientos = ref([])
const loading = ref(false)
const error = ref(null)

const cargarComportamientos = async () => {
  loading.value = true
  error.value = null

  try {
    const url = `${API_ENDPOINTS.COMPORTAMIENTOS_EXTANOS || '/api/comportamientos-extranos'}`
    const response = await authenticatedFetch(url, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })

    if (!response.ok) {
      const data = await response.json().catch(() => ({}))
      error.value = getSafeErrorMessage(response, data)
      logError(new Error(`Error al cargar comportamientos: ${response.status}`), 'ComportamientosExtranos')
      return
    }

    const data = await response.json()
    comportamientos.value = data || []
  } catch (err) {
    error.value = getNetworkErrorMessage(err)
    logError(err, 'ComportamientosExtranos')
  } finally {
    loading.value = false
  }
}

const formatearFecha = (fechaString) => {
  if (!fechaString) return 'N/A'
  
  try {
    const fecha = new Date(fechaString)
    return fecha.toLocaleString('es-ES', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (e) {
    return fechaString
  }
}

onMounted(() => {
  cargarComportamientos()
})
</script>

<style scoped>
.comportamientos-extranos-container {
  padding: 1rem;
}

.header-section {
  margin-bottom: 2rem;
}

.header-section h2 {
  color: #374151;
  margin: 0 0 0.5rem 0;
  font-size: 1.75rem;
  border-bottom: 2px solid #e5e7eb;
  padding-bottom: 0.75rem;
}

.subtitle {
  color: #6b7280;
  margin: 0.5rem 0 0 0;
  font-size: 0.95rem;
}

.loading-state,
.error-state,
.empty-state {
  text-align: center;
  padding: 3rem 2rem;
}

.spinner {
  border: 4px solid #f3f4f6;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-icon,
.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.error-message {
  color: #dc2626;
  margin-bottom: 1rem;
}

.retry-button {
  background: #667eea;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1rem;
  transition: background 0.3s;
}

.retry-button:hover {
  background: #5568d3;
}

.empty-state p {
  color: #6b7280;
  font-size: 1.1rem;
}

.comportamientos-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.comportamiento-card {
  background: #fff;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.comportamiento-card:hover {
  border-color: #fbbf24;
  box-shadow: 0 4px 12px rgba(251, 191, 36, 0.2);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e5e7eb;
}

.card-icon {
  font-size: 2rem;
}

.card-title-section {
  flex: 1;
}

.card-title {
  color: #374151;
  margin: 0 0 0.25rem 0;
  font-size: 1.25rem;
  font-weight: 600;
}

.card-email {
  color: #6b7280;
  margin: 0;
  font-size: 0.9rem;
}

.card-body {
  margin-top: 1rem;
}

.card-description {
  color: #4b5563;
  margin: 0 0 1.5rem 0;
  line-height: 1.6;
  font-size: 0.95rem;
}

.card-details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #f3f4f6;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.detail-label {
  color: #6b7280;
  font-size: 0.875rem;
  font-weight: 500;
}

.detail-value {
  color: #374151;
  font-size: 1rem;
  font-weight: 600;
}

.detail-value.highlight {
  color: #dc2626;
}

.detail-value.warning {
  color: #f59e0b;
}

@media (max-width: 768px) {
  .card-details {
    grid-template-columns: 1fr;
  }
  
  .comportamiento-card {
    padding: 1rem;
  }
}
</style>


