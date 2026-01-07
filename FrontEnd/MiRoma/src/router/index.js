import { createRouter, createWebHistory } from 'vue-router'
import Login from '../components/Login.vue'
import Register from '../components/Register.vue'
import LinkPartner from '../components/LinkPartner.vue'
import { requireAuth, redirectIfAuthenticated } from './authGuard'

const routes = [
  {
    path: '/',
    name: 'Login',
    component: Login,
    beforeEnter: redirectIfAuthenticated
  },
  {
    path: '/login',
    name: 'LoginAlt',
    component: Login,
    beforeEnter: redirectIfAuthenticated
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    beforeEnter: redirectIfAuthenticated
  },
  {
    path: '/link-partner',
    name: 'LinkPartner',
    component: LinkPartner,
    beforeEnter: requireAuth // Proteger esta ruta
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    beforeEnter: requireAuth // Proteger esta ruta
  },
  {
    path: '/dashboard/administrar-ingresos',
    name: 'DashboardAdministrarIngresos',
    component: () => import('../views/Dashboard.vue'),
    beforeEnter: requireAuth
  },
  {
    path: '/dashboard/administrar-egresos',
    name: 'DashboardAdministrarEgresos',
    component: () => import('../views/Dashboard.vue'),
    beforeEnter: requireAuth
  },
  {
    path: '/dashboard/categorias',
    name: 'DashboardCategorias',
    component: () => import('../views/Dashboard.vue'),
    beforeEnter: requireAuth
  },
  {
    path: '/dashboard/presupuestos',
    name: 'DashboardPresupuestos',
    component: () => import('../views/Dashboard.vue'),
    beforeEnter: requireAuth
  },
  {
    path: '/dashboard/presupuestos-categorias',
    name: 'DashboardPresupuestosCategorias',
    component: () => import('../views/Dashboard.vue'),
    beforeEnter: requireAuth
  },
  {
    path: '/dashboard/historial',
    name: 'DashboardHistorial',
    component: () => import('../views/Dashboard.vue'),
    beforeEnter: requireAuth
  },
  {
    path: '/dashboard/resumen',
    name: 'DashboardResumen',
    component: () => import('../views/Dashboard.vue'),
    beforeEnter: requireAuth
  },
  {
    path: '/dashboard/comportamientos-extranos',
    name: 'DashboardComportamientosExtranos',
    component: () => import('../views/Dashboard.vue'),
    beforeEnter: requireAuth
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
