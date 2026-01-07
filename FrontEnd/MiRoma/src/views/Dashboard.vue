<template>
  <div class="dashboard-container">
    <div class="dashboard-header">
      <div class="header-content">
        <div class="user-info">
          <h1>Bienvenido, {{ userData?.nombre || 'Usuario' }}</h1>
          <p class="user-email">{{ userData?.email }}</p>
        </div>
        <LogoutButton />
      </div>
    </div>
    
    <div class="dashboard-content">
      <!-- Menú de opciones principales -->
      <div class="options-menu">
        <h2 class="menu-title">Opciones</h2>
        <div class="menu-grid">
          <div 
            class="menu-card" 
            @click="navigateTo('/dashboard/administrar-ingresos')"
            :class="{ active: currentView === 'administrar-ingresos' }"
          >
            <div class="menu-icon">💰</div>
            <h3>Administrar Ingresos</h3>
            <p>Gestiona y registra tus ingresos</p>
          </div>

          <div 
            class="menu-card" 
            @click="navigateTo('/dashboard/administrar-egresos')"
            :class="{ active: currentView === 'administrar-egresos' }"
          >
            <div class="menu-icon">💸</div>
            <h3>Administrar Egresos</h3>
            <p>Gestiona y registra tus egresos</p>
          </div>

          <div 
            class="menu-card" 
            @click="navigateTo('/dashboard/categorias')"
            :class="{ active: currentView === 'categorias' }"
          >
            <div class="menu-icon">📁</div>
            <h3>Categorías de Egresos</h3>
            <p>Administra tus categorías de gastos</p>
          </div>

          <div 
            class="menu-card" 
            @click="navigateTo('/dashboard/presupuestos')"
            :class="{ active: currentView === 'presupuestos' }"
          >
            <div class="menu-icon">🎯</div>
            <h3>Presupuestos</h3>
            <p>Gestiona tus presupuestos por periodo</p>
          </div>

          <div 
            class="menu-card" 
            @click="navigateTo('/dashboard/presupuestos-categorias')"
            :class="{ active: currentView === 'presupuestos-categorias' }"
          >
            <div class="menu-icon">💼</div>
            <h3>Presupuestos por Categoría</h3>
            <p>Asigna porcentajes de ingresos a categorías</p>
          </div>

          <div 
            class="menu-card" 
            @click="navigateTo('/dashboard/historial')"
            :class="{ active: currentView === 'historial' }"
          >
            <div class="menu-icon">📋</div>
            <h3>Log de Eventos</h3>
            <p>Registro de actividades realizadas por usuario</p>
          </div>

          <div 
            class="menu-card" 
            @click="navigateTo('/dashboard/resumen')"
            :class="{ active: currentView === 'resumen' }"
          >
            <div class="menu-icon">📊</div>
            <h3>Resumen Financiero</h3>
            <p>Vista general de tu situación financiera</p>
          </div>

          <div 
            class="menu-card" 
            @click="navigateTo('/dashboard/comportamientos-extranos')"
            :class="{ active: currentView === 'comportamientos-extranos' }"
          >
            <div class="menu-icon">🚨</div>
            <h3>Comportamientos Extraños</h3>
            <p>Actividades sospechosas detectadas</p>
          </div>
        </div>
      </div>

      <!-- Área de contenido dinámico -->
      <div class="content-area">
        <div v-if="currentView === 'default'" class="welcome-message">
          <div class="welcome-icon">👋</div>
          <h2>Bienvenido a Mi Roma</h2>
          <p>Selecciona una opción del menú para comenzar</p>
        </div>

        <div v-else-if="currentView === 'administrar-ingresos'" class="view-content">
          <AdministrarIngresos />
        </div>

        <div v-else-if="currentView === 'administrar-egresos'" class="view-content">
          <AdministrarEgresos />
        </div>

        <div v-else-if="currentView === 'categorias'" class="view-content">
          <AdministrarCategorias />
        </div>

        <div v-else-if="currentView === 'presupuestos'" class="view-content">
          <AdministrarPresupuestos />
        </div>

        <div v-else-if="currentView === 'presupuestos-categorias'" class="view-content">
          <AdministrarPresupuestosCategorias />
        </div>

        <div v-else-if="currentView === 'historial'" class="view-content">
          <LogEventos />
        </div>

        <div v-else-if="currentView === 'resumen'" class="view-content">
          <ResumenFinanciero />
        </div>

        <div v-else-if="currentView === 'comportamientos-extranos'" class="view-content">
          <ComportamientosExtranos />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getUserData, isAuthenticated as checkAuth } from '../utils/auth'
import LogoutButton from '../components/LogoutButton.vue'
import AdministrarIngresos from '../components/AdministrarIngresos.vue'
import AdministrarEgresos from '../components/AdministrarEgresos.vue'
import AdministrarCategorias from '../components/AdministrarCategorias.vue'
import AdministrarPresupuestos from '../components/AdministrarPresupuestos.vue'
import AdministrarPresupuestosCategorias from '../components/AdministrarPresupuestosCategorias.vue'
import ResumenFinanciero from '../components/ResumenFinanciero.vue'
import LogEventos from '../components/LogEventos.vue'
import ComportamientosExtranos from '../components/ComportamientosExtranos.vue'

const router = useRouter()
const route = useRoute()

const userData = ref(null)

const currentView = computed(() => {
  const path = route.path
  if (path.includes('/administrar-ingresos')) return 'administrar-ingresos'
  if (path.includes('/administrar-egresos')) return 'administrar-egresos'
  if (path.includes('/categorias')) return 'categorias'
  if (path.includes('/presupuestos-categorias')) return 'presupuestos-categorias'
  if (path.includes('/presupuestos')) return 'presupuestos'
  if (path.includes('/historial')) return 'historial'
  if (path.includes('/resumen')) return 'resumen'
  if (path.includes('/comportamientos-extranos')) return 'comportamientos-extranos'
  return 'default'
})

const navigateTo = (path) => {
  router.push(path)
}

onMounted(() => {
  // Verificar autenticación
  if (!checkAuth()) {
    router.push({ name: 'Login' })
    return
  }
  
  // Cargar datos del usuario
  userData.value = getUserData()
})
</script>

<style scoped>
.dashboard-container {
  min-height: 100vh;
  padding: 2rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-attachment: fixed;
}

.dashboard-header {
  background: white;
  padding: 1.5rem 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info h1 {
  color: #667eea;
  margin: 0 0 0.25rem 0;
  font-size: 1.75rem;
}

.user-email {
  color: #6b7280;
  margin: 0;
  font-size: 0.875rem;
}

.dashboard-content {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 2rem;
}

.options-menu {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  height: fit-content;
}

.menu-title {
  color: #374151;
  margin: 0 0 1.5rem 0;
  font-size: 1.25rem;
  border-bottom: 2px solid #e5e7eb;
  padding-bottom: 0.75rem;
}

.menu-grid {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-height: calc(100vh - 300px);
  overflow-y: auto;
}

.menu-grid::-webkit-scrollbar {
  width: 6px;
}

.menu-grid::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.menu-grid::-webkit-scrollbar-thumb {
  background: #667eea;
  border-radius: 10px;
}

.menu-grid::-webkit-scrollbar-thumb:hover {
  background: #764ba2;
}

.menu-card {
  padding: 1.5rem;
  border: 2px solid #e5e7eb;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  justify-content: center;
}

.menu-card:hover {
  border-color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.menu-card.active {
  border-color: #667eea;
  background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
}

.menu-icon {
  font-size: 2.5rem;
  margin-bottom: 0.75rem;
  display: block;
}

.menu-card h3 {
  color: #374151;
  margin: 0 0 0.5rem 0;
  font-size: 1.1rem;
  font-weight: 600;
}

.menu-card p {
  color: #6b7280;
  margin: 0;
  font-size: 0.875rem;
  line-height: 1.4;
}

.content-area {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  min-height: 400px;
}

.welcome-message {
  text-align: center;
  padding: 3rem 2rem;
}

.welcome-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.welcome-message h2 {
  color: #374151;
  margin: 0 0 1rem 0;
  font-size: 2rem;
}

.welcome-message p {
  color: #6b7280;
  font-size: 1.1rem;
}

.view-content h2 {
  color: #374151;
  margin: 0 0 1.5rem 0;
  font-size: 1.75rem;
  border-bottom: 2px solid #e5e7eb;
  padding-bottom: 0.75rem;
}

.placeholder-content {
  text-align: center;
  padding: 3rem 2rem;
}

.placeholder-content p {
  color: #6b7280;
  font-size: 1rem;
  margin: 0.5rem 0;
}

.coming-soon {
  color: #9ca3af;
  font-style: italic;
  margin-top: 1rem !important;
}

@media (max-width: 1024px) {
  .dashboard-content {
    grid-template-columns: 1fr;
  }

  .menu-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 1rem;
  }
}

@media (max-width: 768px) {
  .dashboard-container {
    padding: 1rem;
  }
  
  .dashboard-header {
    padding: 1rem 1.5rem;
  }

  .header-content {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }

  .user-info h1 {
    font-size: 1.5rem;
  }

  .menu-grid {
    grid-template-columns: 1fr;
  }

  .content-area {
    padding: 1.5rem;
  }
}
</style>
