<template>
  <div class="administrar-presupuestos-categorias">
    <div class="header-section">
      <h2>Presupuestos por Categoría</h2>
      <button @click="mostrarFormulario = true" class="btn-primary" v-if="!mostrarFormulario">
        ➕ Nuevo Presupuesto Categoría
      </button>
    </div>

    <!-- Formulario de creación/edición -->
    <div v-if="mostrarFormulario" class="form-section">
      <h3>{{ presupuestoCategoriaEditando ? 'Editar Presupuesto Categoría' : 'Nuevo Presupuesto Categoría' }}</h3>
      <form @submit.prevent="guardarPresupuestoCategoria" class="presupuesto-categoria-form">
        <div class="form-group">
          <label for="presupuesto">Presupuesto <span class="required">*</span></label>
          <select
            id="presupuesto"
            v-model="formData.presupuestoId"
            required
            class="form-select"
            :disabled="presupuestoCategoriaEditando !== null"
          >
            <option value="">Selecciona un presupuesto</option>
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
            <option v-for="categoria in categoriasDisponibles" :key="categoria.id" :value="categoria.id">
              {{ categoria.nombre }} - {{ categoria.descripcion || 'Sin descripción' }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label for="porcentaje">Porcentaje (%) <span class="required">*</span></label>
          <input
            id="porcentaje"
            v-model.number="formData.porcentaje"
            type="number"
            step="0.01"
            min="0.01"
            max="100"
            required
            class="form-input"
            @input="validarPorcentaje"
          />
          <div v-if="porcentajeDisponible !== null" class="porcentaje-info">
            <span :class="{ 'warning': porcentajeDisponible < 10, 'error': porcentajeDisponible < 0 }">
              Porcentaje disponible: {{ porcentajeDisponible.toFixed(2) }}%
            </span>
          </div>
          <div v-if="porcentajeTotal !== null" class="porcentaje-total">
            <span :class="{ 'warning': porcentajeTotal > 90, 'error': porcentajeTotal > 100 }">
              Total asignado: {{ porcentajeTotal.toFixed(2) }}%
            </span>
          </div>
        </div>

        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>

        <div class="form-actions">
          <button type="submit" class="btn-primary" :disabled="isLoading || porcentajeExcedeLimite">
            {{ isLoading ? 'Guardando...' : (presupuestoCategoriaEditando ? 'Actualizar' : 'Crear') }}
          </button>
          <button type="button" @click="cancelarFormulario" class="btn-secondary">
            Cancelar
          </button>
        </div>
      </form>
    </div>

    <!-- Filtro por presupuesto -->
    <div class="filter-section">
      <label for="filtro-presupuesto">Filtrar por presupuesto:</label>
      <select
        id="filtro-presupuesto"
        v-model="presupuestoFiltro"
        class="form-select filter-select"
        @change="cargarPresupuestosCategorias"
      >
        <option value="">Todos los presupuestos</option>
        <option v-for="presupuesto in presupuestos" :key="presupuesto.id" :value="presupuesto.id">
          {{ presupuesto.periodo }}
        </option>
      </select>
    </div>

    <!-- Lista de presupuestos categorías -->
    <div class="presupuestos-categorias-list">
      <div v-if="isLoadingList" class="loading-message">
        Cargando presupuestos por categoría...
      </div>
      <div v-else-if="errorMessageList" class="error-message">
        {{ errorMessageList }}
      </div>
      <div v-else-if="presupuestosCategorias.length === 0" class="empty-state">
        <div class="empty-icon">📊</div>
        <p>No hay presupuestos por categoría registrados</p>
        <p class="empty-hint">Crea un presupuesto por categoría para asignar porcentajes de ingresos a categorías de gastos</p>
        <button @click="mostrarFormulario = true" class="btn-primary">
          Crear primer presupuesto categoría
        </button>
      </div>
      <div v-else class="presupuestos-categorias-grid">
        <div v-for="pc in presupuestosCategorias" :key="pc.id" class="presupuesto-categoria-card">
          <div class="presupuesto-categoria-header">
            <div class="presupuesto-categoria-info">
              <h3>{{ pc.categoriaNombre }}</h3>
              <p class="presupuesto-periodo">{{ pc.presupuestoPeriodo }}</p>
            </div>
            <div class="presupuesto-categoria-actions">
              <button @click="editarPresupuestoCategoria(pc)" class="btn-icon" title="Editar">
                ✏️
              </button>
              <button @click="confirmarEliminar(pc)" class="btn-icon btn-danger" title="Eliminar">
                🗑️
              </button>
            </div>
          </div>
          <div class="presupuesto-categoria-body">
            <div class="presupuesto-categoria-descripcion">
              {{ pc.categoriaDescripcion || 'Sin descripción' }}
            </div>
            <div class="presupuesto-categoria-porcentaje">
              <span class="label">Porcentaje:</span>
              <span class="value">{{ parseFloat(pc.porcentaje).toFixed(2) }}%</span>
            </div>
            <div class="presupuesto-categoria-montos">
              <div class="monto-item">
                <span class="monto-label">Asignado:</span>
                <span class="monto-value asignado">${{ formatCurrency(pc.montoAsignado || 0) }}</span>
              </div>
              <div class="monto-item">
                <span class="monto-label">Gastado:</span>
                <span class="monto-value gastado">${{ formatCurrency(pc.montoGastado || 0) }}</span>
              </div>
              <div class="monto-item disponible">
                <span class="monto-label">Disponible:</span>
                <span class="monto-value" :class="{ 'negativo': (pc.montoDisponible || 0) < 0 }">
                  ${{ formatCurrency(pc.montoDisponible || 0) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal de confirmación de eliminación -->
    <div v-if="presupuestoCategoriaAEliminar" class="modal-overlay" @click="presupuestoCategoriaAEliminar = null">
      <div class="modal-content" @click.stop>
        <h3>Confirmar Eliminación</h3>
        <p>¿Estás seguro de que deseas eliminar este presupuesto por categoría?</p>
        <p class="modal-details">
          <strong>Categoría:</strong> {{ presupuestoCategoriaAEliminar.categoriaNombre }}<br>
          <strong>Presupuesto:</strong> {{ presupuestoCategoriaAEliminar.presupuestoPeriodo }}<br>
          <strong>Porcentaje:</strong> {{ parseFloat(presupuestoCategoriaAEliminar.porcentaje).toFixed(2) }}%
        </p>
        <div class="modal-actions">
          <button @click="eliminarPresupuestoCategoria" class="btn-danger" :disabled="isDeleting">
            {{ isDeleting ? 'Eliminando...' : 'Eliminar' }}
          </button>
          <button @click="presupuestoCategoriaAEliminar = null" class="btn-secondary">
            Cancelar
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { authenticatedFetch } from '../utils/auth'
import { validateAndSanitizeNumber } from '../utils/validation'

import { API_ENDPOINTS } from '../config/api'

const API_BASE_URL = API_ENDPOINTS.PRESUPUESTOS_CATEGORIAS
const API_PRESUPUESTOS = API_ENDPOINTS.PRESUPUESTOS
const API_CATEGORIAS = API_ENDPOINTS.CATEGORIAS

const presupuestosCategorias = ref([])
const presupuestos = ref([])
const categorias = ref([])
const mostrarFormulario = ref(false)
const presupuestoCategoriaEditando = ref(null)
const presupuestoCategoriaAEliminar = ref(null)
const presupuestoFiltro = ref('')
const isLoading = ref(false)
const isLoadingList = ref(false)
const isDeleting = ref(false)
const errorMessage = ref('')
const errorMessageList = ref('')
const porcentajeTotal = ref(null)
const porcentajeDisponible = ref(null)

const formData = ref({
  presupuestoId: '',
  categoriaId: '',
  porcentaje: null
})

const categoriasDisponibles = computed(() => {
  if (!formData.value.presupuestoId) return categorias.value
  
  // Filtrar categorías que ya están asignadas a este presupuesto (excepto la que se está editando)
  const presupuestoId = parseInt(formData.value.presupuestoId)
  const categoriasAsignadas = presupuestosCategorias.value
    .filter(pc => pc.presupuestoId === presupuestoId)
    .filter(pc => !presupuestoCategoriaEditando.value || pc.id !== presupuestoCategoriaEditando.value.id)
    .map(pc => pc.categoriaId)
  
  return categorias.value.filter(cat => !categoriasAsignadas.includes(cat.id))
})

const porcentajeExcedeLimite = computed(() => {
  if (!formData.value.porcentaje || !formData.value.presupuestoId) return false
  return porcentajeTotal.value !== null && porcentajeTotal.value > 100
})

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

const cargarPresupuestosCategorias = async () => {
  isLoadingList.value = true
  errorMessageList.value = ''

  try {
    let url = API_BASE_URL
    if (presupuestoFiltro.value) {
      url = `${API_BASE_URL}/presupuesto/${presupuestoFiltro.value}`
    }

    const response = await authenticatedFetch(url)
    const data = await response.json()

    if (!response.ok) {
      errorMessageList.value = data.error || 'Error al cargar presupuestos por categoría'
      return
    }

    presupuestosCategorias.value = data
  } catch (error) {
    console.error('Error al cargar presupuestos por categoría:', error)
    errorMessageList.value = 'Error de red al cargar presupuestos por categoría'
  } finally {
    isLoadingList.value = false
  }
}

const cargarPorcentajeTotal = async () => {
  if (!formData.value.presupuestoId) {
    porcentajeTotal.value = null
    porcentajeDisponible.value = null
    return
  }

  try {
    const response = await authenticatedFetch(
      `${API_BASE_URL}/presupuesto/${formData.value.presupuestoId}/porcentaje-total`
    )
    const data = await response.json()

    if (response.ok) {
      porcentajeTotal.value = parseFloat(data.porcentajeTotal)
      porcentajeDisponible.value = parseFloat(data.porcentajeDisponible)
    }
  } catch (error) {
    console.error('Error al cargar porcentaje total:', error)
  }
}

const validarPorcentaje = () => {
  if (formData.value.presupuestoId) {
    cargarPorcentajeTotal()
  }
}

watch(() => formData.value.presupuestoId, () => {
  if (formData.value.presupuestoId) {
    cargarPorcentajeTotal()
    // Si se cambia el presupuesto, limpiar la categoría si ya está asignada
    if (formData.value.categoriaId) {
      const categoriaAsignada = presupuestosCategorias.value.find(
        pc => pc.presupuestoId === parseInt(formData.value.presupuestoId) && 
              pc.categoriaId === parseInt(formData.value.categoriaId) &&
              (!presupuestoCategoriaEditando.value || pc.id !== presupuestoCategoriaEditando.value.id)
      )
      if (categoriaAsignada) {
        formData.value.categoriaId = ''
      }
    }
  } else {
    porcentajeTotal.value = null
    porcentajeDisponible.value = null
  }
})

const guardarPresupuestoCategoria = async () => {
  isLoading.value = true
  errorMessage.value = ''

  // Validación frontend
  if (!formData.value.presupuestoId || !formData.value.categoriaId || !formData.value.porcentaje) {
    errorMessage.value = 'Por favor, completa todos los campos'
    isLoading.value = false
    return
  }

  // Validar y sanitizar porcentaje
  const porcentaje = validateAndSanitizeNumber(formData.value.porcentaje)
  if (!porcentaje || porcentaje <= 0 || porcentaje > 100) {
    errorMessage.value = 'El porcentaje debe estar entre 0.01 y 100'
    isLoading.value = false
    return
  }

  // Validar IDs
  const presupuestoId = parseInt(formData.value.presupuestoId)
  const categoriaId = parseInt(formData.value.categoriaId)
  if (!presupuestoId || isNaN(presupuestoId) || !categoriaId || isNaN(categoriaId)) {
    errorMessage.value = 'Por favor, selecciona un presupuesto y una categoría válidos'
    isLoading.value = false
    return
  }

  try {
    const payload = {
      presupuestoId: presupuestoId,
      categoriaId: categoriaId,
      porcentaje: porcentaje
    }

    const url = presupuestoCategoriaEditando.value 
      ? `${API_BASE_URL}/${presupuestoCategoriaEditando.value.id}`
      : API_BASE_URL

    const method = presupuestoCategoriaEditando.value ? 'PUT' : 'POST'

    const response = await authenticatedFetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    })

    const data = await response.json()

    if (!response.ok) {
      errorMessage.value = data.error || 'Error al guardar presupuesto categoría'
      return
    }

    // Recargar lista y cerrar formulario
    await cargarPresupuestosCategorias()
    await cargarPorcentajeTotal()
    cancelarFormulario()
    
    alert(presupuestoCategoriaEditando.value ? 'Presupuesto categoría actualizado exitosamente' : 'Presupuesto categoría creado exitosamente')
  } catch (error) {
    console.error('Error al guardar presupuesto categoría:', error)
    errorMessage.value = 'Error de red al guardar presupuesto categoría'
  } finally {
    isLoading.value = false
  }
}

const editarPresupuestoCategoria = (presupuestoCategoria) => {
  presupuestoCategoriaEditando.value = presupuestoCategoria
  formData.value = {
    presupuestoId: presupuestoCategoria.presupuestoId.toString(),
    categoriaId: presupuestoCategoria.categoriaId.toString(),
    porcentaje: parseFloat(presupuestoCategoria.porcentaje)
  }
  mostrarFormulario.value = true
  cargarPorcentajeTotal()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const cancelarFormulario = () => {
  mostrarFormulario.value = false
  presupuestoCategoriaEditando.value = null
  formData.value = {
    presupuestoId: '',
    categoriaId: '',
    porcentaje: null
  }
  porcentajeTotal.value = null
  porcentajeDisponible.value = null
  errorMessage.value = ''
}

const confirmarEliminar = (presupuestoCategoria) => {
  presupuestoCategoriaAEliminar.value = presupuestoCategoria
}

const eliminarPresupuestoCategoria = async () => {
  if (!presupuestoCategoriaAEliminar.value) return

  isDeleting.value = true

  try {
    const response = await authenticatedFetch(`${API_BASE_URL}/${presupuestoCategoriaAEliminar.value.id}`, {
      method: 'DELETE'
    })

    const data = await response.json()

    if (!response.ok) {
      errorMessage.value = data.error || 'Error al eliminar presupuesto categoría'
      return
    }

    // Recargar lista y cerrar modal
    await cargarPresupuestosCategorias()
    if (formData.value.presupuestoId) {
      await cargarPorcentajeTotal()
    }
    presupuestoCategoriaAEliminar.value = null
    alert('Presupuesto categoría eliminado exitosamente')
  } catch (error) {
    console.error('Error al eliminar presupuesto categoría:', error)
    errorMessage.value = 'Error de red al eliminar presupuesto categoría'
  } finally {
    isDeleting.value = false
  }
}

const formatCurrency = (value) => {
  if (!value && value !== 0) return '0.00'
  return new Intl.NumberFormat('es-CO', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(value)
}

onMounted(async () => {
  await Promise.all([
    cargarPresupuestos(),
    cargarCategorias(),
    cargarPresupuestosCategorias()
  ])
})
</script>

<style scoped>
.administrar-presupuestos-categorias {
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

.presupuesto-categoria-form {
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

.form-select:disabled {
  background-color: #f3f4f6;
  cursor: not-allowed;
}

.porcentaje-info,
.porcentaje-total {
  font-size: 0.875rem;
  margin-top: 0.25rem;
}

.porcentaje-info span,
.porcentaje-total span {
  font-weight: 600;
}

.porcentaje-info .warning,
.porcentaje-total .warning {
  color: #f59e0b;
}

.porcentaje-info .error,
.porcentaje-total .error {
  color: #ef4444;
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

.filter-section {
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.filter-section label {
  font-weight: 500;
  color: #374151;
}

.filter-select {
  min-width: 250px;
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

.presupuestos-categorias-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.presupuesto-categoria-card {
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  padding: 1.5rem;
  transition: all 0.3s ease;
}

.presupuesto-categoria-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.1);
}

.presupuesto-categoria-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e5e7eb;
}

.presupuesto-categoria-info h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #374151;
  margin: 0 0 0.25rem 0;
}

.presupuesto-periodo {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 0;
}

.presupuesto-categoria-actions {
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

.presupuesto-categoria-body {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.presupuesto-categoria-descripcion {
  font-size: 0.875rem;
  color: #6b7280;
  font-style: italic;
}

.presupuesto-categoria-porcentaje {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 0.75rem;
  border-top: 1px solid #f3f4f6;
}

.presupuesto-categoria-porcentaje .label {
  font-weight: 600;
  color: #374151;
}

.presupuesto-categoria-porcentaje .value {
  font-size: 1.125rem;
  font-weight: 700;
  color: #667eea;
}

.presupuesto-categoria-montos {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 2px solid #e5e7eb;
}

.monto-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem;
  border-radius: 6px;
  background: #f9fafb;
}

.monto-item.disponible {
  background: #f0fdf4;
  border: 2px solid #86efac;
}

.monto-label {
  font-weight: 600;
  color: #374151;
  font-size: 0.875rem;
}

.monto-value {
  font-size: 1rem;
  font-weight: 700;
}

.monto-value.asignado {
  color: #3b82f6;
}

.monto-value.gastado {
  color: #ef4444;
}

.monto-item.disponible .monto-value {
  color: #10b981;
}

.monto-value.negativo {
  color: #ef4444;
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

  .presupuestos-categorias-grid {
    grid-template-columns: 1fr;
  }

  .form-actions {
    flex-direction: column;
  }

  .filter-section {
    flex-direction: column;
    align-items: flex-start;
  }

  .filter-select {
    width: 100%;
  }
}
</style>

