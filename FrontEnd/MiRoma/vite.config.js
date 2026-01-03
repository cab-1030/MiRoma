import { fileURLToPath, URL } from 'node:url'
import fs from 'fs'
import path from 'path'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// Rutas de certificados
const certDir = path.resolve(__dirname, 'certs')
const keyPath = path.join(certDir, 'localhost-key.pem')
const certPath = path.join(certDir, 'localhost-cert.pem')

// Configuración HTTPS
let httpsConfig = true // Por defecto, Vite generará certificados automáticamente

// Si existen certificados personalizados, usarlos
if (fs.existsSync(keyPath) && fs.existsSync(certPath)) {
  httpsConfig = {
    key: fs.readFileSync(keyPath),
    cert: fs.readFileSync(certPath),
  }
  console.log('✓ Usando certificados personalizados desde certs/')
} else {
  console.log('ℹ Vite generará certificados automáticamente')
  console.log('  Para usar certificados personalizados, ejecuta: ./generate-frontend-cert.sh')
}

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    https: httpsConfig,
    port: 5173,
    proxy: {
      '/api': {
        target: 'https://localhost:8443',
        changeOrigin: true,
        secure: false, // Permitir certificados autofirmados en desarrollo
        ws: true
      }
    }
  }
})
