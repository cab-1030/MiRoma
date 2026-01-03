<template>
  <div class="administrar-categorias">
    <div class="header-section">
      <h2>Administrar Categorías de Egresos</h2>
      <button @click="mostrarFormulario = true" class="btn-primary" v-if="!mostrarFormulario">
        ➕ Nueva Categoría
      </button>
    </div>

    <!-- Formulario de creación/edición -->
    <div v-if="mostrarFormulario" class="form-section">
      <h3>{{ categoriaEditando ? 'Editar Categoría' : 'Nueva Categoría' }}</h3>
      <form @submit.prevent="guardarCategoria" class="categoria-form">
        <div class="form-group">
          <label for="nombre">Nombre <span class="required">*</span></label>
          <input
            id="nombre"
            v-model="formData.nombre"
            type="text"
            maxlength="200"
            placeholder="Ej: Alimentación, Transporte, etc."
            required
            class="form-input"
          />
          <span class="char-count">{{ formData.nombre?.length || 0 }}/200</span>
        </div>

        <div class="form-group">
          <label for="descripcion">Descripción</label>
          <textarea
            id="descripcion"
            v-model="formData.descripcion"
            rows="3"
            maxlength="500"
            placeholder="Descripción de la categoría (opcional)"
            class="form-input"
          ></textarea>
          <span class="char-count">{{ formData.descripcion?.length || 0 }}/500</span>
        </div>

        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>

        <div class="form-actions">
          <button type="submit" class="btn-primary" :disabled="isLoading">
            {{ isLoading ? 'Guardando...' : (categoriaEditando ? 'Actualizar' : 'Crear') }}
          </button>
          <button type="button" @click="cancelarFormulario" class="btn-secondary">
            Cancelar
          </button>
        </div>
      </form>
    </div>

    <!-- Lista de categorías -->
    <div class="categorias-list">
      <div v-if="isLoadingList" class="loading-message">
        Cargando categorías...
      </div>
      <div v-else-if="categorias.length === 0" class="empty-state">
        <div class="empty-icon">📁</div>
        <p>No hay categorías registradas</p>
        <button @click="mostrarFormulario = true" class="btn-primary">
          Crear primera categoría
        </button>
      </div>
      <div v-else class="categorias-grid">
        <div v-for="categoria in categorias" :key="categoria.id" class="categoria-card">
          <div class="categoria-header">
            <h3 class="categoria-nombre">{{ categoria.nombre }}</h3>
            <div class="categoria-actions">
              <button @click="editarCategoria(categoria)" class="btn-icon" title="Editar">
                ✏️
              </button>
              <button @click="confirmarEliminar(categoria)" class="btn-icon btn-danger" title="Eliminar">
                🗑️
              </button>
            </div>
          </div>
          <div class="categoria-body">
            <p v-if="categoria.descripcion" class="categoria-descripcion">{{ categoria.descripcion }}</p>
            <p v-else class="categoria-descripcion empty">Sin descripción</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal de confirmación de eliminación -->
    <div v-if="categoriaAEliminar" class="modal-overlay" @click="categoriaAEliminar = null">
      <div class="modal-content" @click.stop>
        <h3>Confirmar Eliminación</h3>
        <p>¿Estás seguro de que deseas eliminar esta categoría?</p>
        <p class="modal-details">
          <strong>Nombre:</strong> {{ categoriaAEliminar.nombre }}
        </p>
        <div class="modal-actions">
          <button @click="eliminarCategoria" class="btn-danger" :disabled="isDeleting">
            {{ isDeleting ? 'Eliminando...' : 'Eliminar' }}
          </button>
          <button @click="categoriaAEliminar = null" class="btn-secondary">
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

const API_BASE_URL = API_ENDPOINTS.CATEGORIAS

const categorias = ref([])
const mostrarFormulario = ref(false)
const categoriaEditando = ref(null)
const categoriaAEliminar = ref(null)
const isLoading = ref(false)
const isLoadingList = ref(false)
const isDeleting = ref(false)
const errorMessage = ref('')

const formData = ref({
  nombre: '',
  descripcion: ''
})

const cargarCategorias = async () => {
  isLoadingList.value = true
  errorMessage.value = ''

  try {
    const response = await authenticatedFetch(API_BASE_URL)
    const data = await response.json()

    if (!response.ok) {
      errorMessage.value = data.error || 'Error al cargar categorías'
      return
    }

    categorias.value = data
  } catch (error) {
    console.error('Error al cargar categorías:', error)
    errorMessage.value = 'Error de red al cargar categorías'
  } finally {
    isLoadingList.value = false
  }
}

const guardarCategoria = async () => {
  isLoading.value = true
  errorMessage.value = ''

  // Validar y sanitizar nombre
  const nombreSanitizado = validateAndSanitizeText(formData.value.nombre, 100)
  if (!nombreSanitizado || nombreSanitizado.length < 2) {
    errorMessage.value = 'El nombre debe tener al menos 2 caracteres y no contener caracteres especiales.'
    isLoading.value = false
    return
  }

  // Validar y sanitizar descripción (opcional)
  let descripcionSanitizada = null
  if (formData.value.descripcion && formData.value.descripcion.trim()) {
    descripcionSanitizada = validateAndSanitizeText(formData.value.descripcion, 500)
  }

  try {
    const payload = {
      nombre: nombreSanitizado,
      descripcion: descripcionSanitizada
    }

    const url = categoriaEditando.value 
      ? `${API_BASE_URL}/${categoriaEditando.value.id}`
      : API_BASE_URL

    const method = categoriaEditando.value ? 'PUT' : 'POST'

    const response = await authenticatedFetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    })

    const data = await response.json()

    if (!response.ok) {
      errorMessage.value = data.error || 'Error al guardar categoría'
      return
    }

    // Recargar lista y cerrar formulario
    await cargarCategorias()
    cancelarFormulario()
    
    alert(categoriaEditando.value ? 'Categoría actualizada exitosamente' : 'Categoría creada exitosamente')
  } catch (error) {
    console.error('Error al guardar categoría:', error)
    errorMessage.value = 'Error de red al guardar categoría'
  } finally {
    isLoading.value = false
  }
}

const editarCategoria = (categoria) => {
  categoriaEditando.value = categoria
  formData.value = {
    nombre: categoria.nombre,
    descripcion: categoria.descripcion || ''
  }
  mostrarFormulario.value = true
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const cancelarFormulario = () => {
  mostrarFormulario.value = false
  categoriaEditando.value = null
  formData.value = {
    nombre: '',
    descripcion: ''
  }
  errorMessage.value = ''
}

const confirmarEliminar = (categoria) => {
  categoriaAEliminar.value = categoria
}

const eliminarCategoria = async () => {
  if (!categoriaAEliminar.value) return

  isDeleting.value = true

  try {
    const response = await authenticatedFetch(`${API_BASE_URL}/${categoriaAEliminar.value.id}`, {
      method: 'DELETE'
    })

    const data = await response.json()

    if (!response.ok) {
      errorMessage.value = data.error || 'Error al eliminar categoría'
      return
    }

    // Recargar lista y cerrar modal
    await cargarCategorias()
    categoriaAEliminar.value = null
    alert('Categoría eliminada exitosamente')
  } catch (error) {
    console.error('Error al eliminar categoría:', error)
    errorMessage.value = 'Error de red al eliminar categoría'
  } finally {
    isDeleting.value = false
  }
}

onMounted(() => {
  cargarCategorias()
})
</script>

<style scoped>
.administrar-categorias {
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

.categoria-form {
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

.categorias-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.categoria-card {
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  padding: 1.5rem;
  transition: all 0.3s ease;
}

.categoria-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.1);
}

.categoria-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e5e7eb;
}

.categoria-nombre {
  font-size: 1.25rem;
  font-weight: 600;
  color: #374151;
  margin: 0;
}

.categoria-actions {
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

.categoria-body {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.categoria-descripcion {
  color: #6b7280;
  margin: 0;
  line-height: 1.5;
}

.categoria-descripcion.empty {
  color: #9ca3af;
  font-style: italic;
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

  .categorias-grid {
    grid-template-columns: 1fr;
  }

  .form-actions {
    flex-direction: column;
  }
}
</style>

