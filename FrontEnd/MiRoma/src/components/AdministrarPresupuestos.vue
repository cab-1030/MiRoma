<template>
  <div class="administrar-presupuestos">
    <div class="header-section">
      <h2>Administrar Presupuestos</h2>
      <button @click="mostrarFormulario = true" class="btn-primary" v-if="!mostrarFormulario">
        ➕ Nuevo Presupuesto
      </button>
    </div>

    <!-- Formulario de creación/edición -->
    <div v-if="mostrarFormulario" class="form-section">
      <h3>{{ presupuestoEditando ? 'Editar Presupuesto' : 'Nuevo Presupuesto' }}</h3>
      <form @submit.prevent="guardarPresupuesto" class="presupuesto-form">
        <div class="form-group">
          <label for="periodo">Período (Mes y Año) <span class="required">*</span></label>
          <input
            id="periodo"
            v-model="formData.periodo"
            type="month"
            required
            class="form-input"
          />
          <p class="form-hint">Selecciona el mes y año para este presupuesto</p>
        </div>

        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>

        <div class="form-actions">
          <button type="submit" class="btn-primary" :disabled="isLoading">
            {{ isLoading ? 'Guardando...' : (presupuestoEditando ? 'Actualizar' : 'Crear') }}
          </button>
          <button type="button" @click="cancelarFormulario" class="btn-secondary">
            Cancelar
          </button>
        </div>
      </form>
    </div>

    <!-- Lista de presupuestos -->
    <div class="presupuestos-list">
      <div v-if="isLoadingList" class="loading-message">
        Cargando presupuestos...
      </div>
      <div v-else-if="errorMessageList" class="error-message">
        {{ errorMessageList }}
      </div>
      <div v-else-if="presupuestos.length === 0" class="empty-state">
        <div class="empty-icon">🎯</div>
        <p>No hay presupuestos registrados</p>
        <p class="empty-hint">Crea un presupuesto para comenzar a gestionar tus finanzas por período</p>
        <button @click="mostrarFormulario = true" class="btn-primary">
          Crear primer presupuesto
        </button>
      </div>
      <div v-else class="presupuestos-grid">
        <div v-for="presupuesto in presupuestos" :key="presupuesto.id" class="presupuesto-card">
          <div class="presupuesto-header">
            <div class="presupuesto-periodo">
              <h3>{{ presupuesto.periodo }}</h3>
            </div>
            <div class="presupuesto-actions">
              <button @click="editarPresupuesto(presupuesto)" class="btn-icon" title="Editar">
                ✏️
              </button>
              <button @click="confirmarEliminar(presupuesto)" class="btn-icon btn-danger" title="Eliminar">
                🗑️
              </button>
            </div>
          </div>
          <div class="presupuesto-body">
            <div class="presupuesto-fecha">
              <span class="label">Creado:</span>
              {{ formatDate(presupuesto.fechaCreacion) }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal de confirmación de eliminación -->
    <div v-if="presupuestoAEliminar" class="modal-overlay" @click="presupuestoAEliminar = null">
      <div class="modal-content" @click.stop>
        <h3>Confirmar Eliminación</h3>
        <p>¿Estás seguro de que deseas eliminar este presupuesto?</p>
        <p class="modal-details">
          <strong>Período:</strong> {{ presupuestoAEliminar.periodo }}
        </p>
        <div class="modal-actions">
          <button @click="eliminarPresupuesto" class="btn-danger" :disabled="isDeleting">
            {{ isDeleting ? 'Eliminando...' : 'Eliminar' }}
          </button>
          <button @click="presupuestoAEliminar = null" class="btn-secondary">
            Cancelar
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { authenticatedFetch } from '../utils/auth'
import { validateAndSanitizeText } from '../utils/validation'

import { API_ENDPOINTS } from '../config/api'

const API_BASE_URL = API_ENDPOINTS.PRESUPUESTOS

const presupuestos = ref([])
const mostrarFormulario = ref(false)
const presupuestoEditando = ref(null)
const presupuestoAEliminar = ref(null)
const isLoading = ref(false)
const isLoadingList = ref(false)
const isDeleting = ref(false)
const errorMessage = ref('')
const errorMessageList = ref('')

const formData = ref({
  periodo: ''
})

const cargarPresupuestos = async () => {
  isLoadingList.value = true
  errorMessageList.value = ''

  try {
    const response = await authenticatedFetch(API_BASE_URL)
    const data = await response.json()

    if (!response.ok) {
      errorMessageList.value = data.error || 'Error al cargar presupuestos'
      return
    }

    presupuestos.value = data
  } catch (error) {
    console.error('Error al cargar presupuestos:', error)
    errorMessageList.value = 'Error de red al cargar presupuestos'
  } finally {
    isLoadingList.value = false
  }
}

const guardarPresupuesto = async () => {
  isLoading.value = true
  errorMessage.value = ''

  // Validar formato de período (YYYY-MM)
  if (!formData.value.periodo || !/^\d{4}-\d{2}$/.test(formData.value.periodo)) {
    errorMessage.value = 'Por favor, selecciona un período válido (mes y año).'
    isLoading.value = false
    return
  }

  try {
    // Formatear el período: convertir YYYY-MM a formato legible (Ej: "Enero 2025")
    const periodoFormateado = formatearPeriodo(formData.value.periodo)
    
    // Sanitizar el período formateado
    const periodoSanitizado = validateAndSanitizeText(periodoFormateado, 50)
    
    const payload = {
      periodo: periodoSanitizado
    }

    const url = presupuestoEditando.value 
      ? `${API_BASE_URL}/${presupuestoEditando.value.id}`
      : API_BASE_URL

    const method = presupuestoEditando.value ? 'PUT' : 'POST'

    const response = await authenticatedFetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    })

    const data = await response.json()

    if (!response.ok) {
      errorMessage.value = data.error || 'Error al guardar presupuesto'
      return
    }

    // Recargar lista y cerrar formulario
    await cargarPresupuestos()
    cancelarFormulario()
    
    alert(presupuestoEditando.value ? 'Presupuesto actualizado exitosamente' : 'Presupuesto creado exitosamente')
  } catch (error) {
    console.error('Error al guardar presupuesto:', error)
    errorMessage.value = 'Error de red al guardar presupuesto'
  } finally {
    isLoading.value = false
  }
}

const editarPresupuesto = (presupuesto) => {
  presupuestoEditando.value = presupuesto
  // Convertir el período almacenado (ej: "Enero 2025") al formato YYYY-MM para el datepicker
  const periodoDate = convertirPeriodoADate(presupuesto.periodo)
  formData.value = {
    periodo: periodoDate
  }
  mostrarFormulario.value = true
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const cancelarFormulario = () => {
  mostrarFormulario.value = false
  presupuestoEditando.value = null
  formData.value = {
    periodo: ''
  }
  errorMessage.value = ''
}

const confirmarEliminar = (presupuesto) => {
  presupuestoAEliminar.value = presupuesto
}

const eliminarPresupuesto = async () => {
  if (!presupuestoAEliminar.value) return

  isDeleting.value = true

  try {
    const response = await authenticatedFetch(`${API_BASE_URL}/${presupuestoAEliminar.value.id}`, {
      method: 'DELETE'
    })

    const data = await response.json()

    if (!response.ok) {
      errorMessage.value = data.error || 'Error al eliminar presupuesto'
      return
    }

    // Recargar lista y cerrar modal
    await cargarPresupuestos()
    presupuestoAEliminar.value = null
    alert('Presupuesto eliminado exitosamente')
  } catch (error) {
    console.error('Error al eliminar presupuesto:', error)
    errorMessage.value = 'Error de red al eliminar presupuesto'
  } finally {
    isDeleting.value = false
  }
}

const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('es-CO', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// Formatear período de YYYY-MM a formato legible (Ej: "Enero 2025")
const formatearPeriodo = (periodoYYYYMM) => {
  if (!periodoYYYYMM) return ''
  
  // Si ya está en formato legible, retornarlo
  if (!periodoYYYYMM.match(/^\d{4}-\d{2}$/)) {
    return periodoYYYYMM
  }
  
  const [year, month] = periodoYYYYMM.split('-')
  const date = new Date(parseInt(year), parseInt(month) - 1, 1)
  
  return date.toLocaleDateString('es-CO', {
    year: 'numeric',
    month: 'long'
  })
}

// Convertir período legible (Ej: "Enero 2025") a formato YYYY-MM para el datepicker
const convertirPeriodoADate = (periodo) => {
  if (!periodo) return ''
  
  // Si ya está en formato YYYY-MM, retornarlo
  if (periodo.match(/^\d{4}-\d{2}$/)) {
    return periodo
  }
  
  // Intentar parsear diferentes formatos
  try {
    // Formato: "Enero 2025" o "enero 2025"
    const meses = {
      'enero': '01', 'febrero': '02', 'marzo': '03', 'abril': '04',
      'mayo': '05', 'junio': '06', 'julio': '07', 'agosto': '08',
      'septiembre': '09', 'octubre': '10', 'noviembre': '11', 'diciembre': '12'
    }
    
    const partes = periodo.toLowerCase().trim().split(/\s+/)
    let mes = null
    let año = null
    
    // Buscar mes y año
    for (const parte of partes) {
      if (meses[parte]) {
        mes = meses[parte]
      } else if (/^\d{4}$/.test(parte)) {
        año = parte
      }
    }
    
    if (mes && año) {
      return `${año}-${mes}`
    }
    
    // Si no se puede parsear, intentar con Date
    const date = new Date(periodo)
    if (!isNaN(date.getTime())) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      return `${year}-${month}`
    }
  } catch (e) {
    console.warn('No se pudo convertir el período:', periodo)
  }
  
  // Si no se puede convertir, retornar el mes actual
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  return `${year}-${month}`
}

onMounted(() => {
  cargarPresupuestos()
})
</script>

<style scoped>
.administrar-presupuestos {
  width: 100%;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.header-section h2 {
  margin: 0;
}

.btn-primary {
  padding: 0.75rem 1.5rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-secondary {
  padding: 0.75rem 1.5rem;
  background: #e5e7eb;
  color: #374151;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-secondary:hover {
  background: #d1d5db;
}

.form-section {
  background: #f9fafb;
  padding: 1.5rem;
  border-radius: 12px;
  margin-bottom: 2rem;
}

.form-section h3 {
  margin: 0 0 1.5rem 0;
  color: #374151;
}

.presupuesto-form {
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
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s ease;
  font-family: inherit;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-hint {
  font-size: 0.75rem;
  color: #6b7280;
  margin: 0;
  font-style: italic;
}

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 0.5rem;
}

.error-message {
  padding: 0.75rem 1rem;
  background-color: #fee2e2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  color: #dc2626;
  font-size: 0.875rem;
}

.loading-message {
  text-align: center;
  padding: 3rem;
  color: #6b7280;
}

.empty-state {
  text-align: center;
  padding: 3rem 2rem;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.empty-state p {
  color: #6b7280;
  margin-bottom: 0.5rem;
}

.empty-hint {
  font-size: 0.875rem;
  color: #9ca3af;
  margin-bottom: 1.5rem !important;
}

.presupuestos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.presupuesto-card {
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  padding: 1.5rem;
  transition: all 0.3s ease;
}

.presupuesto-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.1);
}

.presupuesto-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e5e7eb;
}

.presupuesto-periodo h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #374151;
  margin: 0;
}

.presupuesto-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-icon {
  background: none;
  border: none;
  font-size: 1.25rem;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.btn-icon:hover {
  background: #f3f4f6;
}

.btn-icon.btn-danger:hover {
  background: #fee2e2;
}

.presupuesto-body {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.presupuesto-fecha {
  font-size: 0.875rem;
  color: #6b7280;
}

.presupuesto-fecha .label {
  font-weight: 600;
  margin-right: 0.5rem;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  max-width: 400px;
  width: 90%;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.modal-content h3 {
  margin: 0 0 1rem 0;
  color: #374151;
}

.modal-content p {
  color: #6b7280;
  margin: 0.5rem 0;
}

.modal-details {
  background: #f9fafb;
  padding: 1rem;
  border-radius: 8px;
  margin: 1rem 0;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1.5rem;
}

.btn-danger {
  padding: 0.75rem 1.5rem;
  background: #ef4444;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-danger:hover:not(:disabled) {
  background: #dc2626;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.4);
}

.btn-danger:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .header-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .presupuestos-grid {
    grid-template-columns: 1fr;
  }

  .form-actions {
    flex-direction: column;
  }
}
</style>

