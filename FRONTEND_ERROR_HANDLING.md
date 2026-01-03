# Guía de Actualización de Manejo de Errores en Frontend

## Componentes Actualizados

Los siguientes componentes ya usan el nuevo sistema de manejo de errores:
- ✅ `Login.vue`
- ✅ `Register.vue`
- ✅ `LinkPartner.vue`
- ✅ `AdministrarIngresos.vue` (parcialmente)

## Componentes Pendientes de Actualizar

Los siguientes componentes necesitan ser actualizados para usar el nuevo sistema:

### 1. AdministrarCategorias.vue
```javascript
// Agregar import
import { getSafeErrorMessage, getNetworkErrorMessage, logError } from '../utils/errorHandler'

// Reemplazar en catch blocks:
// Antes:
errorMessage.value = data.error || 'Error al guardar categoría'
console.error('Error al guardar categoría:', error)

// Después:
errorMessage.value = await getSafeErrorMessage(response, data)
logError(error, 'AdministrarCategorias')
errorMessage.value = getNetworkErrorMessage(error)
```

### 2. AdministrarEgresos.vue
```javascript
// Agregar import
import { getSafeErrorMessage, getNetworkErrorMessage, logError } from '../utils/errorHandler'

// Aplicar en todos los catch blocks
```

### 3. AdministrarPresupuestos.vue
```javascript
// Agregar import
import { getSafeErrorMessage, getNetworkErrorMessage, logError } from '../utils/errorHandler'

// Aplicar en todos los catch blocks
```

### 4. AdministrarPresupuestosCategorias.vue
```javascript
// Agregar import
import { getSafeErrorMessage, getNetworkErrorMessage, logError } from '../utils/errorHandler'

// Aplicar en todos los catch blocks
```

### 5. LogEventos.vue
```javascript
// Agregar import
import { getSafeErrorMessage, getNetworkErrorMessage, logError } from '../utils/errorHandler'

// Aplicar en todos los catch blocks
```

### 6. ResumenFinanciero.vue
```javascript
// Agregar import
import { getSafeErrorMessage, getNetworkErrorMessage, logError } from '../utils/errorHandler'

// Aplicar en todos los catch blocks
```

## Patrón de Actualización

Para cada componente, seguir este patrón:

### Paso 1: Agregar imports
```javascript
import { getSafeErrorMessage, getNetworkErrorMessage, logError } from '../utils/errorHandler'
```

### Paso 2: Actualizar manejo de errores en respuestas HTTP
```javascript
// Antes:
if (!response.ok) {
  errorMessage.value = data.error || 'Error genérico'
  return
}

// Después:
if (!response.ok) {
  errorMessage.value = await getSafeErrorMessage(response, data)
  logError(new Error(`Error: ${response.status}`), 'NombreComponente')
  return
}
```

### Paso 3: Actualizar catch blocks
```javascript
// Antes:
catch (error) {
  console.error('Error:', error)
  errorMessage.value = 'Error de red'
}

// Después:
catch (error) {
  logError(error, 'NombreComponente')
  errorMessage.value = getNetworkErrorMessage(error)
}
```

## Beneficios

1. **Seguridad**: No se revela información sensible al usuario
2. **Consistencia**: Todos los errores se manejan de la misma forma
3. **Logging**: Errores se registran para debugging
4. **UX**: Mensajes de error claros y útiles para el usuario

