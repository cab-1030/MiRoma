# Configuración de Base de Datos

## Variables de Entorno

Para configurar la conexión a la base de datos de forma segura, debes establecer las siguientes variables de entorno:

```bash
DB_HOST=localhost
DB_PORT=3306
DB_NAME=mi_roma
DB_USER=mi_roma_app
DB_PASSWORD=MiRomaApp_2025!
```

## Formas de Configurar las Variables de Entorno

### Opción 1: Variables de Entorno del Sistema (Recomendado)

**En Linux/Mac:**
```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=mi_roma
export DB_USER=mi_roma_app
export DB_PASSWORD=MiRomaApp_2025!
```

**En Windows (PowerShell):**
```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="3306"
$env:DB_NAME="mi_roma"
$env:DB_USER="mi_roma_app"
$env:DB_PASSWORD="MiRomaApp_2025!"
```

**En Windows (CMD):**
```cmd
set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=mi_roma
set DB_USER=mi_roma_app
set DB_PASSWORD=MiRomaApp_2025!
```

### Opción 2: Archivo .env (usando spring-dotenv)

Si prefieres usar un archivo `.env`, puedes agregar la dependencia `spring-dotenv` al proyecto.

### Opción 3: application.properties (Solo para desarrollo local)

⚠️ **NO RECOMENDADO PARA PRODUCCIÓN**

Si no se configuran las variables de entorno, la aplicación usará los valores por defecto definidos en `application.properties`.

## Probar la Conexión

Una vez configuradas las variables de entorno, puedes probar la conexión usando el endpoint:

```
GET http://localhost:8080/api/db/test
```

Este endpoint devolverá información sobre la conexión a la base de datos si todo está configurado correctamente.

## Seguridad

- **NUNCA** subas las credenciales de la base de datos al repositorio
- Usa variables de entorno en producción
- Considera usar un servicio de gestión de secretos (como AWS Secrets Manager, Azure Key Vault, etc.) en entornos de producción

