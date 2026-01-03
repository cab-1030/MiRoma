<template>
  <div class="administrar-ingresos">
    <div class="header-section">
      <h2>Administrar Ingresos</h2>
      <button @click="mostrarFormulario = true" class="btn-primary" v-if="!mostrarFormulario">
        ➕ Nuevo Ingreso
      </button>
    </div>

    <!-- Formulario de creación/edición -->
    <div v-if="mostrarFormulario" class="form-section">
      <h3>{{ ingresoEditando ? 'Editar Ingreso' : 'Nuevo Ingreso' }}</h3>
      <form @submit.prevent="guardarIngreso" class="ingreso-form">
        <div class="form-group">
          <label for="monto">Monto (Pesos Colombianos) <span class="required">*</span></label>
          <input
            id="monto"
            v-model="formData.monto"
            type="number"
            step="0.01"
            min="0.01"
            placeholder="0.00"
            required
            class="form-input"
          />
        </div>

        <div class="form-group">
          <label for="descripcion">Descripción</label>
          <textarea
            id="descripcion"
            v-model="formData.descripcion"
            rows="3"
            maxlength="500"
            placeholder="Descripción del ingreso (opcional)"
            class="form-input"
          ></textarea>
          <span class="char-count">{{ formData.descripcion?.length || 0 }}/500</span>
        </div>

        <div class="form-group">
          <label for="fecha">Fecha <span class="required">*</span></label>
          <input
            id="fecha"
            v-model="formData.fecha"
            type="date"
            required
            class="form-input"
          />
        </div>

        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>

        <div class="form-actions">
          <button type="submit" class="btn-primary" :disabled="isLoading">
            {{ isLoading ? 'Guardando...' : (ingresoEditando ? 'Actualizar' : 'Crear') }}
          </button>
          <button type="button" @click="cancelarFormulario" class="btn-secondary">
            Cancelar
          </button>
        </div>
      </form>
    </div>

    <!-- Lista de ingresos -->
    <div class="ingresos-list">
      <div v-if="isLoadingList" class="loading-message">
        Cargando ingresos...
      </div>
      <div v-else-if="ingresos.length === 0" class="empty-state">
        <div class="empty-icon">💰</div>
        <p>No hay ingresos registrados</p>
        <button @click="mostrarFormulario = true" class="btn-primary">
          Crear primer ingreso
        </button>
      </div>
      <div v-else class="ingresos-grid">
        <div v-for="ingreso in ingresos" :key="ingreso.id" class="ingreso-card">
          <div class="ingreso-header">
            <div class="ingreso-monto">
              <span class="currency">$</span>
              {{ formatCurrency(ingreso.monto) }}
            </div>
            <div class="ingreso-actions">
              <button @click="editarIngreso(ingreso)" class="btn-icon" title="Editar">
                ✏️
              </button>
              <button @click="confirmarEliminar(ingreso)" class="btn-icon btn-danger" title="Eliminar">
                🗑️
              </button>
            </div>
          </div>
          <div class="ingreso-body">
            <p v-if="ingreso.descripcion" class="ingreso-descripcion">{{ ingreso.descripcion }}</p>
            <p v-else class="ingreso-descripcion empty">Sin descripción</p>
            <div class="ingreso-fecha">
              <span class="label">Fecha:</span>
              {{ formatDate(ingreso.fecha) }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal de confirmación de eliminación -->
    <div v-if="ingresoAEliminar" class="modal-overlay" @click="ingresoAEliminar = null">
      <div class="modal-content" @click.stop>
        <h3>Confirmar Eliminación</h3>
        <p>¿Estás seguro de que deseas eliminar este ingreso?</p>
        <p class="modal-details">
          <strong>Monto:</strong> ${{ formatCurrency(ingresoAEliminar.monto) }}<br>
          <strong>Fecha:</strong> {{ formatDate(ingresoAEliminar.fecha) }}
        </p>
        <div class="modal-actions">
          <button @click="eliminarIngreso" class="btn-danger" :disabled="isDeleting">
            {{ isDeleting ? 'Eliminando...' : 'Eliminar' }}
          </button>
          <button @click="ingresoAEliminar = null" class="btn-secondary">
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
import { validateAndSanitizeNumber, validateAndSanitizeText, validateAndSanitizeDate } from '../utils/validation'
import { getSafeErrorMessage, getNetworkErrorMessage, logError } from '../utils/errorHandler'

import { API_ENDPOINTS } from '../config/api'

const API_BASE_URL = API_ENDPOINTS.INGRESOS

const ingresos = ref([])
const mostrarFormulario = ref(false)
const ingresoEditando = ref(null)
const ingresoAEliminar = ref(null)
const isLoading = ref(false)
const isLoadingList = ref(false)
const isDeleting = ref(false)
const errorMessage = ref('')

const formData = ref({
  monto: '',
  descripcion: '',
  fecha: new Date().toISOString().split('T')[0] // Fecha actual por defecto
})

const cargarIngresos = async () => {
  isLoadingList.value = true
  errorMessage.value = ''

  try {
    const response = await authenticatedFetch(API_BASE_URL)
    const data = await response.json()

    if (!response.ok) {
      errorMessage.value = data.error || 'Error al cargar ingresos'
      return
    }

    ingresos.value = data
  } catch (error) {
    logError(error, 'AdministrarIngresos')
    errorMessage.value = getNetworkErrorMessage(error)
  } finally {
    isLoadingList.value = false
  }
}

const guardarIngreso = async () => {
  isLoading.value = true
  errorMessage.value = ''

  // Validar y sanitizar monto
  const montoValidado = validateAndSanitizeNumber(formData.value.monto)
  if (!montoValidado || montoValidado <= 0) {
    errorMessage.value = 'Por favor, ingresa un monto válido mayor a cero.'
    isLoading.value = false
    return
  }

  // Validar y sanitizar descripción (opcional)
  let descripcionSanitizada = null
  if (formData.value.descripcion && formData.value.descripcion.trim()) {
    descripcionSanitizada = validateAndSanitizeText(formData.value.descripcion, 500)
  }

  // Validar fecha
  const fechaValidada = validateAndSanitizeDate(formData.value.fecha)
  if (!fechaValidada) {
    errorMessage.value = 'Por favor, ingresa una fecha válida.'
    isLoading.value = false
    return
  }

  try {
    const payload = {
      monto: montoValidado,
      descripcion: descripcionSanitizada,
      fecha: fechaValidada
    }

    const url = ingresoEditando.value 
      ? `${API_BASE_URL}/${ingresoEditando.value.id}`
      : API_BASE_URL

    const method = ingresoEditando.value ? 'PUT' : 'POST'

    const response = await authenticatedFetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    })

    const data = await response.json()

    if (!response.ok) {
      errorMessage.value = await getSafeErrorMessage(response, data)
      logError(new Error(`Error saving income: ${response.status}`), 'AdministrarIngresos')
      return
    }

    // Recargar lista y cerrar formulario
    await cargarIngresos()
    cancelarFormulario()
    
    alert(ingresoEditando.value ? 'Ingreso actualizado exitosamente' : 'Ingreso creado exitosamente')
  } catch (error) {
    logError(error, 'AdministrarIngresos')
    errorMessage.value = getNetworkErrorMessage(error)
  } finally {
    isLoading.value = false
  }
}

const editarIngreso = (ingreso) => {
  ingresoEditando.value = ingreso
  formData.value = {
    monto: ingreso.monto.toString(),
    descripcion: ingreso.descripcion || '',
    fecha: ingreso.fecha
  }
  mostrarFormulario.value = true
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const cancelarFormulario = () => {
  mostrarFormulario.value = false
  ingresoEditando.value = null
  formData.value = {
    monto: '',
    descripcion: '',
    fecha: new Date().toISOString().split('T')[0]
  }
  errorMessage.value = ''
}

const confirmarEliminar = (ingreso) => {
  ingresoAEliminar.value = ingreso
}

const eliminarIngreso = async () => {
  if (!ingresoAEliminar.value) return

  isDeleting.value = true

  try {
    const response = await authenticatedFetch(`${API_BASE_URL}/${ingresoAEliminar.value.id}`, {
      method: 'DELETE'
    })

    const data = await response.json()

    if (!response.ok) {
      errorMessage.value = await getSafeErrorMessage(response, data)
      logError(new Error(`Error deleting income: ${response.status}`), 'AdministrarIngresos')
      return
    }

    // Recargar lista y cerrar modal
    await cargarIngresos()
    ingresoAEliminar.value = null
    alert('Ingreso eliminado exitosamente')
  } catch (error) {
    logError(error, 'AdministrarIngresos')
    errorMessage.value = getNetworkErrorMessage(error)
  } finally {
    isDeleting.value = false
  }
}

const formatCurrency = (value) => {
  return new Intl.NumberFormat('es-CO', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(value)
}

const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('es-CO', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

onMounted(() => {
  cargarIngresos()
})
</script>

<style scoped>
.administrar-ingresos {
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

.ingreso-form {
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

textarea.form-input {
  resize: vertical;
  min-height: 80px;
}

.char-count {
  font-size: 0.75rem;
  color: #6b7280;
  text-align: right;
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
  margin-bottom: 1.5rem;
}

.ingresos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.ingreso-card {
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  padding: 1.5rem;
  transition: all 0.3s ease;
}

.ingreso-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.1);
}

.ingreso-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e5e7eb;
}

.ingreso-monto {
  font-size: 1.5rem;
  font-weight: 700;
  color: #10b981;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.currency {
  font-size: 1.25rem;
}

.ingreso-actions {
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

.ingreso-body {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.ingreso-descripcion {
  color: #374151;
  margin: 0;
  line-height: 1.5;
}

.ingreso-descripcion.empty {
  color: #9ca3af;
  font-style: italic;
}

.ingreso-fecha {
  font-size: 0.875rem;
  color: #6b7280;
}

.ingreso-fecha .label {
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

  .ingresos-grid {
    grid-template-columns: 1fr;
  }

  .form-actions {
    flex-direction: column;
  }
}
</style>

