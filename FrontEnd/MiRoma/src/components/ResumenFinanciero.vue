<template>
  <div class="resumen-financiero">
    <div class="header-section">
      <h2>Resumen Financiero</h2>
      <p class="subtitle">Resumen de ingresos y egresos por período (filtrado por usuario)</p>
    </div>

    <div v-if="isLoading" class="loading-state">
      <div class="loading-spinner">⏳</div>
      <p>Cargando resumen financiero...</p>
    </div>

    <div v-else-if="errorMessage" class="error-state">
      <div class="error-icon">⚠️</div>
      <p class="error-message">{{ errorMessage }}</p>
      <button @click="cargarResumen" class="btn-primary">Reintentar</button>
    </div>

    <div v-else-if="resumen.length === 0" class="empty-state">
      <div class="empty-icon">📊</div>
      <p>No hay períodos registrados</p>
      <p class="empty-hint">Crea un presupuesto para comenzar a ver tu resumen financiero</p>
    </div>

    <div v-else class="resumen-grid">
      <div v-for="item in resumen" :key="item.presupuestoId" class="periodo-card">
        <div class="periodo-header">
          <h3>{{ item.periodo }}</h3>
        </div>
        <div class="periodo-body">
          <div class="monto-item ingresos">
            <div class="monto-label">
              <span class="monto-icon">💰</span>
              <span>Ingresos Totales</span>
            </div>
            <div class="monto-value positive">
              ${{ formatCurrency(item.ingresosTotales) }}
            </div>
          </div>

          <div class="monto-item egresos">
            <div class="monto-label">
              <span class="monto-icon">💸</span>
              <span>Egresos Totales</span>
            </div>
            <div class="monto-value negative">
              ${{ formatCurrency(item.egresosTotales) }}
            </div>
          </div>

          <div class="monto-item restante" :class="{ 'negativo': item.dineroRestante < 0 }">
            <div class="monto-label">
              <span class="monto-icon">{{ item.dineroRestante >= 0 ? '✅' : '⚠️' }}</span>
              <span>Dinero Restante</span>
            </div>
            <div class="monto-value" :class="{ 'positive': item.dineroRestante >= 0, 'negative': item.dineroRestante < 0 }">
              ${{ formatCurrency(item.dineroRestante) }}
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

import { API_ENDPOINTS } from '../config/api'

const API_BASE_URL = API_ENDPOINTS.RESUMEN_FINANCIERO

const resumen = ref([])
const isLoading = ref(false)
const errorMessage = ref('')

const formatCurrency = (value) => {
  if (!value) return '0.00'
  return new Intl.NumberFormat('es-CO', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(value)
}

const cargarResumen = async () => {
  isLoading.value = true
  errorMessage.value = ''

  try {
    const response = await authenticatedFetch(API_BASE_URL)
    const data = await response.json()

    if (!response.ok) {
      errorMessage.value = data.error || 'Error al cargar resumen financiero'
      return
    }

    resumen.value = data
  } catch (error) {
    console.error('Error al cargar resumen:', error)
    errorMessage.value = 'Error de red al cargar resumen financiero'
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  cargarResumen()
})
</script>

<style scoped>
.resumen-financiero {
  padding: 0;
}

.header-section {
  margin-bottom: 2rem;
}

.header-section h2 {
  color: #374151;
  margin: 0 0 0.5rem 0;
  font-size: 1.75rem;
}

.subtitle {
  color: #6b7280;
  margin: 0;
  font-size: 0.875rem;
}

.loading-state,
.error-state,
.empty-state {
  text-align: center;
  padding: 3rem 2rem;
}

.loading-spinner,
.error-icon,
.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.error-message {
  color: #dc2626;
  margin: 1rem 0;
}

.resumen-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.periodo-card {
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  padding: 1.5rem;
  transition: all 0.3s ease;
}

.periodo-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
  transform: translateY(-2px);
}

.periodo-header {
  border-bottom: 2px solid #e5e7eb;
  padding-bottom: 1rem;
  margin-bottom: 1rem;
}

.periodo-header h3 {
  color: #374151;
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
}

.periodo-body {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.monto-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  border-radius: 8px;
  background: #f9fafb;
}

.monto-item.ingresos {
  background: #ecfdf5;
}

.monto-item.egresos {
  background: #fef2f2;
}

.monto-item.restante {
  background: #eff6ff;
  border: 2px solid #dbeafe;
}

.monto-item.restante.negativo {
  background: #fef2f2;
  border-color: #fecaca;
}

.monto-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #374151;
  font-weight: 500;
  font-size: 0.875rem;
}

.monto-icon {
  font-size: 1.25rem;
}

.monto-value {
  font-size: 1.125rem;
  font-weight: 700;
}

.monto-value.positive {
  color: #059669;
}

.monto-value.negative {
  color: #dc2626;
}

.btn-primary {
  background: #667eea;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 1rem;
}

.btn-primary:hover {
  background: #5568d3;
  transform: translateY(-1px);
}

.empty-hint {
  color: #9ca3af;
  font-size: 0.875rem;
  margin-top: 0.5rem;
}

@media (max-width: 768px) {
  .resumen-grid {
    grid-template-columns: 1fr;
  }
}
</style>

