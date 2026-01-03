# Troubleshooting - API no encontrada

## Problema: No se encuentra la API /api/auth/register

### Pasos para verificar:

1. **Verificar que el servidor esté corriendo:**
   ```bash
   cd BackEnd/miroma
   ./mvnw spring-boot:run
   ```
   
   Deberías ver mensajes como:
   ```
   Started MiromaApplication in X.XXX seconds
   ```

2. **Verificar que el servidor esté en el puerto correcto:**
   - El servidor debe estar en `http://localhost:8080`
   - Verifica en los logs: `Tomcat started on port(s): 8080`

3. **Probar el endpoint de prueba:**
   ```bash
   curl http://localhost:8080/api/auth/test
   ```
   
   Debería responder con:
   ```json
   {
     "message": "AuthController está funcionando correctamente",
     "endpoint": "/api/auth/register"
   }
   ```

4. **Verificar que no haya errores al iniciar:**
   - Revisa los logs del servidor para ver si hay errores
   - Busca mensajes como "Error creating bean" o "Failed to start"

5. **Verificar la conexión a la base de datos:**
   ```bash
   curl http://localhost:8080/api/db/test
   ```
   
   Si este endpoint funciona, la aplicación está corriendo correctamente.

### Soluciones comunes:

1. **Si el servidor no inicia:**
   - Verifica que MySQL esté corriendo
   - Verifica las variables de entorno de la base de datos
   - Revisa los logs para ver el error específico

2. **Si el endpoint no responde:**
   - Verifica que el controlador esté en el paquete correcto (`com.miroma.miroma.controller`)
   - Asegúrate de que `@SpringBootApplication` esté escaneando el paquete base
   - Reinicia el servidor

3. **Si hay errores de CORS:**
   - El controlador ya tiene `@CrossOrigin(origins = "*")`
   - Verifica que `CorsConfig.java` esté configurado correctamente

### Verificar endpoints disponibles:

Una vez que el servidor esté corriendo, puedes verificar los endpoints disponibles en los logs. Spring Boot muestra los endpoints mapeados al iniciar.

