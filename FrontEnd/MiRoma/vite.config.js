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
        ws: true,
        // Configurar reescritura de cookies para que funcionen con el proxy
        cookieDomainRewrite: '', // Mantener el dominio de las cookies (sin dominio específico)
        cookiePathRewrite: '/', // Mantener el path de las cookies
        // Asegurar que las cookies se reenvíen al backend
        xfwd: true, // Agregar headers X-Forwarded-*
        logLevel: 'debug', // Para debugging
        configure: (proxy, _options) => {
          // Log de cookies enviadas desde el navegador
          proxy.on('proxyReq', (proxyReq, req, res) => {
            const cookieHeader = req.headers.cookie
            if (cookieHeader) {
              console.log(`[PROXY REQ] ${req.method} ${req.url} - Cookies enviadas desde navegador:`, cookieHeader)
              // Asegurar que las cookies se reenvíen al backend
              // El proxy debería hacerlo automáticamente, pero lo verificamos explícitamente
              if (!proxyReq.getHeader('cookie')) {
                proxyReq.setHeader('cookie', cookieHeader)
              }
            } else {
              console.log(`[PROXY REQ] ${req.method} ${req.url} - NO hay cookies en la petición desde el navegador`)
            }
          })
          
          proxy.on('proxyRes', (proxyRes, req, res) => {
            // Asegurar que las cookies se reenvíen correctamente
            const setCookieHeaders = proxyRes.headers['set-cookie']
            if (setCookieHeaders) {
              console.log(`[PROXY RES] ${req.method} ${req.url} - Cookies recibidas del backend:`, setCookieHeaders)
              // Modificar las cookies para que funcionen con el proxy
              const modifiedCookies = setCookieHeaders.map(cookie => {
                // Remover dominio específico y ajustar Secure si es necesario
                let modifiedCookie = cookie
                  .replace(/Domain=[^;]+;?/gi, '') // Remover Domain
                  .replace(/;\s*Secure/gi, '') // Remover Secure en desarrollo
                  .replace(/;\s*SameSite=Strict/gi, '; SameSite=Lax') // Cambiar Strict a Lax
                
                // Limpiar espacios dobles y comas múltiples
                modifiedCookie = modifiedCookie.replace(/\s+/g, ' ').replace(/;\s*;/g, ';').trim()
                
                return modifiedCookie
              })
              proxyRes.headers['set-cookie'] = modifiedCookies
              console.log(`[PROXY RES] ${req.method} ${req.url} - Cookies modificadas enviadas al frontend:`, modifiedCookies)
            } else {
              console.log(`[PROXY RES] ${req.method} ${req.url} - No se recibieron cookies del backend`)
            }
          })
        }
      }
    }
  }
})
