# Errores Comunes y Soluciones

## Error: "Schema-validation: missing column"

**Causa:** La entidad espera una columna que no existe en la tabla.

**Solución:** 
- Verifica que la tabla `usuarios` tenga todas las columnas necesarias
- Ejecuta el script SQL para crear/actualizar la tabla

## Error: "Schema-validation: wrong column type"

**Causa:** El tipo de dato en la entidad no coincide con el de la base de datos.

**Solución:**
- Verifica que `id` sea `INT` en la base de datos (no `BIGINT`)
- Verifica que `salario` sea `DOUBLE` en la base de datos
- Verifica que `fecha_creacion` sea `DATETIME` en la base de datos

## Error: "Cannot determine embedded database driver class"

**Causa:** Problema con la configuración del driver de MySQL.

**Solución:** Ya está configurado en `application.properties`. Verifica que MySQL esté corriendo.

## Error: "Access denied for user"

**Causa:** Credenciales incorrectas o usuario sin permisos.

**Solución:**
- Verifica las variables de entorno de la base de datos
- Verifica que el usuario `mi_roma_app` tenga permisos en la base de datos `mi_roma`

## Error: "Table 'usuarios' doesn't exist"

**Causa:** La tabla no existe en la base de datos.

**Solución:** Ejecuta el script SQL para crear la tabla:
```sql
CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    pareja_id VARCHAR(255) NULL,
    rol_id INT NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    salario DOUBLE NOT NULL,
    activo INT NOT NULL DEFAULT 1,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_pareja_id (pareja_id),
    CONSTRAINT chk_rol_id CHECK (rol_id IN (1, 2))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

## Error: "BeanCreationException" o "Failed to start"

**Causa:** Problema con la inyección de dependencias o configuración.

**Solución:**
- Verifica que todos los servicios tengan `@Service`
- Verifica que todos los repositorios tengan `@Repository`
- Verifica que todos los controladores tengan `@RestController`
- Revisa los logs completos para ver el error específico

## Para obtener el error completo:

1. Inicia el servidor con:
   ```bash
   cd BackEnd/miroma
   ./mvnw spring-boot:run
   ```

2. Copia el mensaje de error completo que aparece en la consola
3. Busca líneas que contengan "ERROR", "Exception", o "Failed"

