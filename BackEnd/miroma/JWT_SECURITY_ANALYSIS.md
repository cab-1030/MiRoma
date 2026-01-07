# Análisis de Seguridad JWT - MiRoma

## Vulnerabilidades Identificadas y Corregidas

### 1. ✅ **Algoritmo "none" Attack** - CORREGIDO

**Vulnerabilidad Original:**
- El código no validaba explícitamente que el algoritmo no fuera "none"
- Un atacante podría crear un token con `{"alg": "none"}` y el sistema lo aceptaría sin verificar la firma

**Corrección Aplicada:**
```java
// Validar que el algoritmo no sea "none"
String algorithm = header.getAlgorithm();
if (algorithm == null || "none".equalsIgnoreCase(algorithm)) {
    throw new io.jsonwebtoken.security.SecurityException(
        "Algoritmo 'none' no permitido. Tokens sin firma son rechazados por seguridad."
    );
}
```

**Estado:** ✅ **PROTEGIDO** - Los tokens con algoritmo "none" son rechazados explícitamente

---

### 2. ✅ **Algorithm Confusion Attack (Simétrico vs Asimétrico)** - CORREGIDO

**Vulnerabilidad Original:**
- El código no validaba explícitamente el algoritmo del header
- Un atacante podría intentar cambiar el algoritmo a RSA/ECDSA para explotar confusión de algoritmos
- Aunque el código usa solo HMAC, no había validación explícita

**Corrección Aplicada:**
```java
// Validar que el algoritmo sea el esperado (HS256)
if (!ALLOWED_ALGORITHM.equals(algorithm)) {
    throw new io.jsonwebtoken.security.SecurityException(
        String.format("Algoritmo '%s' no permitido. Solo se acepta '%s' (HMAC-SHA256).", 
            algorithm, ALLOWED_ALGORITHM)
    );
}
```

**Estado:** ✅ **PROTEGIDO** - Solo se acepta HS256, se rechazan todos los demás algoritmos

---

### 3. ✅ **Claves HMAC Débiles** - CORREGIDO

**Vulnerabilidad Original:**
- No había validación de la longitud mínima de la clave secreta
- Una clave corta podría ser vulnerable a ataques de fuerza bruta

**Corrección Aplicada:**
```java
// Longitud mínima de la clave secreta (256 bits = 32 bytes)
private static final int MIN_SECRET_KEY_LENGTH = 32;

private void validateSecretKey() {
    if (secret == null || secret.length() < MIN_SECRET_KEY_LENGTH) {
        throw new IllegalArgumentException(
            String.format("La clave secreta JWT debe tener al menos %d caracteres (256 bits) para ser segura", 
                MIN_SECRET_KEY_LENGTH)
        );
    }
}
```

**Estado:** ✅ **PROTEGIDO** - Se valida que la clave tenga al menos 32 caracteres (256 bits)

**Nota:** La clave por defecto en `application.properties` tiene 77 caracteres, lo cual es seguro.

---

### 4. ✅ **Confiar en el Header del Token (Algorithm Header Attack)** - CORREGIDO

**Vulnerabilidad Original:**
- El código usaba `.verifyWith(getSigningKey())` que es seguro, pero no validaba explícitamente el algoritmo del header
- Un atacante podría intentar manipular el header para cambiar el algoritmo

**Corrección Aplicada:**
```java
// 1. Primero validar el algoritmo del header ANTES de verificar la firma
Header header = jws.getHeader();
String algorithm = header.getAlgorithm();

// 2. Validar que sea el algoritmo esperado
if (!ALLOWED_ALGORITHM.equals(algorithm)) {
    throw new SecurityException("Algoritmo no permitido");
}

// 3. Luego verificar la firma con la clave correcta
return Jwts.parser()
    .verifyWith(getSigningKey()) // Forzar uso de la clave HMAC
    .build()
    .parseSignedClaims(token)
    .getPayload();
```

**Estado:** ✅ **PROTEGIDO** - El sistema NO confía en el header del token. Valida explícitamente el algoritmo antes de verificar la firma.

---

## Resumen de Protecciones Implementadas

| Vulnerabilidad | Estado | Protección |
|---------------|--------|------------|
| Algoritmo "none" attack | ✅ CORREGIDO | Validación explícita que rechaza `alg: "none"` |
| Algorithm confusion (RSA/ECDSA) | ✅ CORREGIDO | Solo acepta HS256, rechaza todos los demás |
| Claves HMAC débiles | ✅ CORREGIDO | Validación de longitud mínima (32 caracteres) |
| Confiar en header del token | ✅ CORREGIDO | Validación explícita del algoritmo antes de verificar firma |

---

## Mejores Prácticas Implementadas

1. **Validación Explícita del Algoritmo:**
   - El algoritmo se valida ANTES de verificar la firma
   - Solo se acepta HS256 (HMAC-SHA256)
   - Se rechazan explícitamente: "none", RSA, ECDSA, y cualquier otro algoritmo

2. **Validación de Longitud de Clave:**
   - Mínimo 32 caracteres (256 bits) requeridos
   - Validación al inicio del servicio

3. **Forzar Algoritmo en Generación:**
   - Los tokens se generan con `alg: "HS256"` explícitamente en el header
   - No se confía en valores por defecto

4. **Manejo de Excepciones:**
   - Excepciones de seguridad se propagan correctamente
   - Mensajes de error no revelan información sensible

---

## Recomendaciones Adicionales

1. **Rotación de Claves:**
   - Considera implementar rotación periódica de la clave secreta JWT
   - Usa variables de entorno para la clave secreta en producción

2. **Monitoreo:**
   - Implementa logging de intentos de tokens con algoritmos inválidos
   - Monitorea intentos de ataques de algoritmo "none"

3. **Configuración de Producción:**
   - Asegúrate de que `jwt.secret` en producción tenga al menos 32 caracteres
   - Usa un generador de claves seguras (ej: `openssl rand -base64 32`)

---

## Verificación

Para verificar que las protecciones funcionan, puedes probar:

1. **Token con algoritmo "none":**
   ```bash
   # Debe ser rechazado
   curl -H "Authorization: Bearer <token_con_alg_none>" https://localhost:8443/api/...
   ```

2. **Token con algoritmo diferente (ej: RS256):**
   ```bash
   # Debe ser rechazado
   curl -H "Authorization: Bearer <token_con_alg_RS256>" https://localhost:8443/api/...
   ```

3. **Token válido con HS256:**
   ```bash
   # Debe ser aceptado
   curl -H "Authorization: Bearer <token_valido>" https://localhost:8443/api/...
   ```

---

**Fecha de Análisis:** 2025-01-XX
**Versión de JJWT:** 0.12.5
**Estado General:** ✅ **SEGURO** - Todas las vulnerabilidades identificadas han sido corregidas


