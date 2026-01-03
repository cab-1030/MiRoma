<template>
  <div class="administrar-egresos">
    <div class="header-section">
      <h2>Administrar Egresos</h2>
      <button @click="mostrarFormulario = true" class="btn-primary" v-if="!mostrarFormulario">
        ➕ Nuevo Egreso
      </button>
    </div>

    <!-- Formulario de creación/edición -->
    <div v-if="mostrarFormulario" class="form-section">
      <h3>{{ egresoEditando ? 'Editar Egreso' : 'Nuevo Egreso' }}</h3>
      <form @submit.prevent="guardarEgreso" class="egreso-form">
        <div class="form-group">
          <label for="montoTotal">Monto Total (Pesos Colombianos) <span class="required">*</span></label>
          <input
            id="montoTotal"
            v-model="formData.montoTotal"
            type="number"
            step="0.01"
            min="0.01"
            placeholder="0.00"
            required
            class="form-input"
          />
        </div>

        <div class="form-group">
          <label for="periodo">Período <span class="required">*</span></label>
          <select
            id="periodo"
            v-model="formData.periodoId"
            required
            class="form-select"
          >
            <option value="">Selecciona un período</option>
            <option v-for="presupuesto in presupuestos" :key="presupuesto.id" :value="presupuesto.id">
              {{ presupuesto.periodo }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label for="categoria">Categoría <span class="required">*</span></label>
          <select
            id="categoria"
            v-model="formData.categoriaId"
            required
            class="form-select"
          >
            <option value="">Selecciona una categoría</option>
            <option v-for="categoria in categorias" :key="categoria.id" :value="categoria.id">
              {{ categoria.nombre }} - {{ categoria.descripcion || 'Sin descripción' }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label for="descripcion">Descripción</label>
          <textarea
            id="descripcion"
            v-model="formData.descripcion"
            rows="3"
            maxlength="500"
            placeholder="Descripción del egreso (opcional)"
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
            {{ isLoading ? 'Guardando...' : (egresoEditando ? 'Actualizar' : 'Crear') }}
          </button>
          <button type="button" @click="cancelarFormulario" class="btn-secondary">
            Cancelar
          </button>
        </div>
      </form>
    </div>

    <!-- Filtro por fecha -->
    <div class="filter-section">
      <label for="filtro-mes">Filtrar por mes:</label>
      <input
        id="filtro-mes"
        v-model="filtroMes"
        type="month"
        class="form-select filter-select"
        @change="cargarEgresos"
      />
      <button @click="limpiarFiltro" class="btn-secondary btn-small" v-if="filtroMes">
        Ver todos
      </button>
    </div>

    <!-- Lista de egresos -->
    <div class="egresos-list">
      <div v-if="isLoadingList" class="loading-message">
        Cargando egresos...
      </div>
      <div v-else-if="errorMessageList" class="error-message">
        {{ errorMessageList }}
      </div>
      <div v-else-if="egresos.length === 0" class="empty-state">
        <div class="empty-icon">💸</div>
        <p v-if="filtroMes">No hay egresos registrados para el mes seleccionado</p>
        <p v-else>No hay egresos registrados</p>
        <p class="empty-hint">Los egresos se distribuirán automáticamente entre los miembros de la pareja según sus ingresos</p>
        <button @click="mostrarFormulario = true" class="btn-primary">
          Crear primer egreso
        </button>
      </div>
      <div v-else class="egresos-grid">
        <div v-for="egreso in egresos" :key="egreso.id" class="egreso-card">
          <div class="egreso-header">
            <div class="egreso-monto">
              <span class="currency">$</span>
              {{ formatCurrency(egreso.montoTotal) }}
            </div>
            <div class="egreso-actions">
              <button @click="editarEgreso(egreso)" class="btn-icon" title="Editar">
                ✏️
              </button>
              <button @click="confirmarEliminar(egreso)" class="btn-icon btn-danger" title="Eliminar">
                🗑️
              </button>
            </div>
          </div>
          <div class="egreso-body">
            <div class="egreso-categoria">
              <span class="label">Categoría:</span>
              <span class="value">{{ egreso.categoriaNombre }}</span>
            </div>
            <div class="egreso-periodo">
              <span class="label">Período:</span>
              <span class="value">{{ egreso.periodoNombre }}</span>
            </div>
            <p v-if="egreso.descripcion" class="egreso-descripcion">{{ egreso.descripcion }}</p>
            <p v-else class="egreso-descripcion empty">Sin descripción</p>
            <div class="egreso-fecha">
              <span class="label">Fecha:</span>
              {{ formatDate(egreso.fecha) }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal de confirmación de eliminación -->
    <div v-if="egresoAEliminar" class="modal-overlay" @click="egresoAEliminar = null">
      <div class="modal-content" @click.stop>
        <h3>Confirmar Eliminación</h3>
        <p>¿Estás seguro de que deseas eliminar este egreso?</p>
        <p class="modal-details">
          <strong>Monto:</strong> ${{ formatCurrency(egresoAEliminar.montoTotal) }}<br>
          <strong>Categoría:</strong> {{ egresoAEliminar.categoriaNombre }}<br>
          <strong>Período:</strong> {{ egresoAEliminar.periodoNombre }}<br>
          <strong>Fecha:</strong> {{ formatDate(egresoAEliminar.fecha) }}
        </p>
        <div class="modal-actions">
          <button @click="eliminarEgreso" class="btn-danger" :disabled="isDeleting">
            {{ isDeleting ? 'Eliminando...' : 'Eliminar' }}
          </button>
          <button @click="egresoAEliminar = null" class="btn-secondary">
            Cancelar
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { authenticatedFetch } from '../utils/auth'
import { validateAndSanitizeNumber, validateAndSanitizeText, validateAndSanitizeDate } from '../utils/validation'

import { API_ENDPOINTS } from '../config/api'

const API_BASE_URL = API_ENDPOINTS.EGRESOS
const API_CATEGORIAS = API_ENDPOINTS.CATEGORIAS
const API_PRESUPUESTOS = API_ENDPOINTS.PRESUPUESTOS

const egresos = ref([])
const categorias = ref([])
const presupuestos = ref([])
const mostrarFormulario = ref(false)
const egresoEditando = ref(null)
const egresoAEliminar = ref(null)
const isLoading = ref(false)
const isLoadingList = ref(false)
const isDeleting = ref(false)
const errorMessage = ref('')
const errorMessageList = ref('')
const filtroMes = ref('')

const formData = ref({
  montoTotal: '',
  categoriaId: '',
  periodoId: '',
  descripcion: '',
  fecha: new Date().toISOString().split('T')[0] // Fecha actual por defecto
})

const cargarCategorias = async () => {
  try {
    const response = await authenticatedFetch(API_CATEGORIAS)
    const data = await response.json()
    if (response.ok) {
      categorias.value = data
    }
  } catch (error) {
    console.error('Error al cargar categorías:', error)
  }
}

const cargarPresupuestos = async () => {
  try {
    const response = await authenticatedFetch(API_PRESUPUESTOS)
    const data = await response.json()
    if (response.ok) {
      presupuestos.value = data
    }
  } catch (error) {
    console.error('Error al cargar presupuestos:', error)
  }
}

const obtenerFechasDelMes = (mesAnio) => {
  if (!mesAnio) return null
  
  const [anio, mes] = mesAnio.split('-')
  const fechaInicio = new Date(parseInt(anio), parseInt(mes) - 1, 1)
  const fechaFin = new Date(parseInt(anio), parseInt(mes), 0) // Último día del mes
  
  return {
    inicio: fechaInicio.toISOString().split('T')[0],
    fin: fechaFin.toISOString().split('T')[0]
  }
}

const cargarEgresos = async () => {
  isLoadingList.value = true
  errorMessageList.value = ''

  try {
    let url = API_BASE_URL
    
    // Si hay filtro de mes, agregar parámetros de fecha
    if (filtroMes.value) {
      const fechas = obtenerFechasDelMes(filtroMes.value)
      if (fechas) {
        url += `?fechaInicio=${fechas.inicio}&fechaFin=${fechas.fin}`
      }
    }

    const response = await authenticatedFetch(url)
    const data = await response.json()

    if (!response.ok) {
      errorMessageList.value = data.error || 'Error al cargar egresos'
      return
    }

    egresos.value = data
  } catch (error) {
    console.error('Error al cargar egresos:', error)
    errorMessageList.value = 'Error de red al cargar egresos'
  } finally {
    isLoadingList.value = false
  }
}

const limpiarFiltro = () => {
  filtroMes.value = ''
  cargarEgresos()
}

const guardarEgreso = async () => {
  isLoading.value = true
  errorMessage.value = ''

  // Validar y sanitizar montoTotal
  const montoTotalValidado = validateAndSanitizeNumber(formData.value.montoTotal)
  if (!montoTotalValidado || montoTotalValidado <= 0) {
    errorMessage.value = 'Por favor, ingresa un monto válido mayor a cero.'
    isLoading.value = false
    return
  }

  // Validar categoriaId
  const categoriaId = parseInt(formData.value.categoriaId)
  if (!categoriaId || isNaN(categoriaId)) {
    errorMessage.value = 'Por favor, selecciona una categoría.'
    isLoading.value = false
    return
  }

  // Validar periodoId
  const periodoId = parseInt(formData.value.periodoId)
  if (!periodoId || isNaN(periodoId)) {
    errorMessage.value = 'Por favor, selecciona un período.'
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
      montoTotal: montoTotalValidado,
      categoriaId: categoriaId,
      periodoId: periodoId,
      descripcion: descripcionSanitizada,
      fecha: fechaValidada
    }

    const url = egresoEditando.value 
      ? `${API_BASE_URL}/${egresoEditando.value.id}`
      : API_BASE_URL

    const method = egresoEditando.value ? 'PUT' : 'POST'

    const response = await authenticatedFetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    })

    const data = await response.json()

    if (!response.ok) {
      errorMessage.value = data.error || 'Error al guardar egreso'
      return
    }

    // Recargar lista y cerrar formulario
    await cargarEgresos()
    cancelarFormulario()
    
    alert(egresoEditando.value ? 'Egreso actualizado exitosamente' : 'Egreso creado exitosamente. Las participaciones se han calculado automáticamente.')
  } catch (error) {
    console.error('Error al guardar egreso:', error)
    errorMessage.value = 'Error de red al guardar egreso'
  } finally {
    isLoading.value = false
  }
}

const editarEgreso = (egreso) => {
  egresoEditando.value = egreso
  formData.value = {
    montoTotal: egreso.montoTotal.toString(),
    categoriaId: egreso.categoriaId.toString(),
    descripcion: egreso.descripcion || '',
    fecha: egreso.fecha
  }
  mostrarFormulario.value = true
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const cancelarFormulario = () => {
  mostrarFormulario.value = false
  egresoEditando.value = null
  formData.value = {
    montoTotal: '',
    categoriaId: '',
    periodoId: '',
    descripcion: '',
    fecha: new Date().toISOString().split('T')[0]
  }
  errorMessage.value = ''
}

const confirmarEliminar = (egreso) => {
  egresoAEliminar.value = egreso
}

const eliminarEgreso = async () => {
  if (!egresoAEliminar.value) return

  isDeleting.value = true

  try {
    const response = await authenticatedFetch(`${API_BASE_URL}/${egresoAEliminar.value.id}`, {
      method: 'DELETE'
    })

    const data = await response.json()

    if (!response.ok) {
      errorMessage.value = data.error || 'Error al eliminar egreso'
      return
    }

    // Recargar lista y cerrar modal
    await cargarEgresos()
    egresoAEliminar.value = null
    alert('Egreso eliminado exitosamente')
  } catch (error) {
    console.error('Error al eliminar egreso:', error)
    errorMessage.value = 'Error de red al eliminar egreso'
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

onMounted(async () => {
  // Establecer el mes actual por defecto
  const ahora = new Date()
  const mesActual = `${ahora.getFullYear()}-${String(ahora.getMonth() + 1).padStart(2, '0')}`
  filtroMes.value = mesActual
  
  await Promise.all([
    cargarCategorias(),
    cargarPresupuestos(),
    cargarEgresos()
  ])
})
</script>

<style scoped>
.administrar-egresos {
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

.egreso-form {
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

.form-input,
.form-select {
  padding: 0.875rem 1rem;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s ease;
  font-family: inherit;
}

.form-input:focus,
.form-select:focus {
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
  margin-bottom: 0.5rem;
}

.empty-hint {
  font-size: 0.875rem;
  color: #9ca3af;
  margin-bottom: 1.5rem !important;
}

.egresos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.egreso-card {
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  padding: 1.5rem;
  transition: all 0.3s ease;
}

.egreso-card:hover {
  border-color: #ef4444;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.1);
}

.egreso-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e5e7eb;
}

.egreso-monto {
  font-size: 1.5rem;
  font-weight: 700;
  color: #ef4444;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.currency {
  font-size: 1.25rem;
}

.egreso-actions {
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

.egreso-body {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.egreso-categoria {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem;
  background: #f3f4f6;
  border-radius: 6px;
}

.egreso-categoria .label {
  font-weight: 600;
  color: #374151;
  font-size: 0.875rem;
}

.egreso-categoria .value {
  font-weight: 500;
  color: #667eea;
  font-size: 0.875rem;
}

.egreso-periodo {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem;
  background: #fef3c7;
  border-radius: 6px;
}

.egreso-periodo .label {
  font-weight: 600;
  color: #374151;
  font-size: 0.875rem;
}

.egreso-periodo .value {
  font-weight: 500;
  color: #d97706;
  font-size: 0.875rem;
}

.egreso-descripcion {
  color: #374151;
  margin: 0;
  line-height: 1.5;
}

.egreso-descripcion.empty {
  color: #9ca3af;
  font-style: italic;
}

.egreso-fecha {
  font-size: 0.875rem;
  color: #6b7280;
}

.egreso-fecha .label {
  font-weight: 600;
  margin-right: 0.5rem;
}

.filter-section {
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.filter-section label {
  font-weight: 500;
  color: #374151;
}

.filter-select {
  min-width: 200px;
  padding: 0.875rem 1rem;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s ease;
  font-family: inherit;
}

.filter-select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.btn-small {
  padding: 0.5rem 1rem;
  font-size: 0.875rem;
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
  line-height: 1.6;
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

  .egresos-grid {
    grid-template-columns: 1fr;
  }

  .form-actions {
    flex-direction: column;
  }
}
</style>

