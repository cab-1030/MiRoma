<template>
  <div class="log-eventos">
    <div class="header-section">
      <h2>Log de Eventos</h2>
      <p class="subtitle">Registro de actividades realizadas por usuario</p>
    </div>

    <div v-if="isLoading" class="loading-state">
      <div class="loading-spinner">⏳</div>
      <p>Cargando eventos...</p>
    </div>

    <div v-else-if="errorMessage" class="error-state">
      <div class="error-icon">⚠️</div>
      <p class="error-message">{{ errorMessage }}</p>
      <button @click="cargarEventos" class="btn-primary">Reintentar</button>
    </div>

    <!-- Filtros -->
    <div v-if="!isLoading && !errorMessage" class="filters-section">
      <div class="filters-grid">
        <div class="filter-group">
          <label for="fecha-inicio">Fecha Inicio</label>
          <input
            id="fecha-inicio"
            v-model="filtroFechaInicio"
            type="date"
            class="filter-input"
            @change="aplicarFiltros"
          />
        </div>

        <div class="filter-group">
          <label for="fecha-fin">Fecha Fin</label>
          <input
            id="fecha-fin"
            v-model="filtroFechaFin"
            type="date"
            class="filter-input"
            @change="aplicarFiltros"
          />
        </div>

        <div class="filter-group">
          <label for="tipo-accion">Tipo de Acción</label>
          <select
            id="tipo-accion"
            v-model="filtroTipoAccion"
            class="filter-select"
            @change="aplicarFiltros"
          >
            <option value="">Todos los tipos</option>
            <option value="Creó">Creó</option>
            <option value="Actualizó">Actualizó</option>
            <option value="Eliminó">Eliminó</option>
            <option value="Asignó">Asignó</option>
          </select>
        </div>

        <div class="filter-actions">
          <button @click="limpiarFiltros" class="btn-secondary">
            Limpiar Filtros
          </button>
        </div>
      </div>
    </div>

    <div v-if="eventosFiltrados.length === 0 && !isLoading" class="empty-state">
      <div class="empty-icon">📋</div>
      <p v-if="tieneFiltros">No hay eventos que coincidan con los filtros seleccionados</p>
      <p v-else>No hay eventos registrados</p>
      <p class="empty-hint">Las acciones que realices en la plataforma aparecerán aquí</p>
    </div>

    <div v-else-if="eventosFiltrados.length > 0" class="eventos-list">
      <div v-for="evento in eventosFiltrados" :key="evento.id" class="evento-card">
        <div class="evento-header">
          <div class="evento-fecha">
            <span class="fecha-icon">📅</span>
            <span>{{ formatFecha(evento.fecha) }}</span>
          </div>
        </div>
        <div class="evento-body">
          <p class="evento-accion">{{ evento.accion }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { authenticatedFetch } from '../utils/auth'

import { API_ENDPOINTS } from '../config/api'

const API_BASE_URL = API_ENDPOINTS.LOG_EVENTOS

const eventos = ref([])
const eventosFiltrados = ref([])
const isLoading = ref(false)
const errorMessage = ref('')
const filtroFechaInicio = ref('')
const filtroFechaFin = ref('')
const filtroTipoAccion = ref('')

const formatFecha = (fecha) => {
  if (!fecha) return ''
  const date = new Date(fecha)
  return new Intl.DateTimeFormat('es-CO', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  }).format(date)
}

const tieneFiltros = computed(() => {
  return filtroFechaInicio.value || filtroFechaFin.value || filtroTipoAccion.value
})

const cargarEventos = async () => {
  isLoading.value = true
  errorMessage.value = ''

  try {
    // Construir URL con parámetros de filtro
    let url = API_BASE_URL
    const params = []
    
    if (filtroFechaInicio.value) {
      params.push(`fechaInicio=${filtroFechaInicio.value}`)
    }
    if (filtroFechaFin.value) {
      params.push(`fechaFin=${filtroFechaFin.value}`)
    }
    if (filtroTipoAccion.value) {
      params.push(`tipoAccion=${encodeURIComponent(filtroTipoAccion.value)}`)
    }
    
    if (params.length > 0) {
      url += '?' + params.join('&')
    }

    const response = await authenticatedFetch(url)
    const data = await response.json()

    if (!response.ok) {
      errorMessage.value = data.error || 'Error al cargar eventos'
      return
    }

    eventos.value = data
    eventosFiltrados.value = data
  } catch (error) {
    console.error('Error al cargar eventos:', error)
    errorMessage.value = 'Error de red al cargar eventos'
  } finally {
    isLoading.value = false
  }
}

const aplicarFiltros = () => {
  cargarEventos()
}

const limpiarFiltros = () => {
  filtroFechaInicio.value = ''
  filtroFechaFin.value = ''
  filtroTipoAccion.value = ''
  cargarEventos()
}

onMounted(() => {
  cargarEventos()
})
</script>

<style scoped>
.log-eventos {
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

.eventos-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-height: calc(100vh - 300px);
  overflow-y: auto;
}

.eventos-list::-webkit-scrollbar {
  width: 6px;
}

.eventos-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.eventos-list::-webkit-scrollbar-thumb {
  background: #667eea;
  border-radius: 10px;
}

.evento-card {
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 10px;
  padding: 1.25rem;
  transition: all 0.3s ease;
}

.evento-card:hover {
  border-color: #667eea;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.1);
}

.evento-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid #e5e7eb;
}

.evento-fecha {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #6b7280;
  font-size: 0.875rem;
}

.fecha-icon {
  font-size: 1rem;
}

.evento-body {
  color: #374151;
}

.evento-accion {
  margin: 0;
  font-size: 0.9375rem;
  line-height: 1.5;
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

.filters-section {
  background: #f9fafb;
  border: 2px solid #e5e7eb;
  border-radius: 10px;
  padding: 1.5rem;
  margin-bottom: 2rem;
}

.filters-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  align-items: end;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.filter-group label {
  color: #374151;
  font-weight: 600;
  font-size: 0.875rem;
}

.filter-input,
.filter-select {
  padding: 0.75rem;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 0.875rem;
  transition: all 0.3s ease;
  background: white;
}

.filter-input:focus,
.filter-select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.filter-actions {
  display: flex;
  align-items: flex-end;
}

.btn-secondary {
  background: #6b7280;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.875rem;
}

.btn-secondary:hover {
  background: #4b5563;
  transform: translateY(-1px);
}

@media (max-width: 768px) {
  .filters-grid {
    grid-template-columns: 1fr;
  }

  .evento-card {
    padding: 1rem;
  }
}
</style>

