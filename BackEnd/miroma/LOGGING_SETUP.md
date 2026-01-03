# Configuración de Logging para Producción

## Sistema de Logging Implementado

El sistema incluye un mecanismo de logging asíncrono que almacena los logs en archivos locales y está preparado para integrarse con servicios de logging externos.

## Configuración Actual

### Logging Local
- Los logs se almacenan en: `~/miroma-logs/`
- Formato: JSON por línea
- Rotación: Un archivo por día
- Procesamiento: Cada 30 segundos

### Estructura de Logs
```json
{
  "timestamp": "2025-01-15T10:30:00",
  "level": "ERROR",
  "message": "Mensaje de error",
  "context": "CustomException: /api/ingresos",
  "exception": "com.miroma.miroma.exception.ValidationException",
  "exceptionMessage": "Detalles del error"
}
```

## Integración con Servicios Externos

### Opción 1: ELK Stack (Elasticsearch, Logstash, Kibana)

1. **Agregar dependencia en `pom.xml`:**
```xml
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```

2. **Configurar `logback-spring.xml`:**
```xml
<configuration>
    <appender name="STASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
        <destination>localhost:5000</destination>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder" />
    </appender>
    <root level="INFO">
        <appender-ref ref="STASH" />
    </root>
</configuration>
```

### Opción 2: AWS CloudWatch

1. **Agregar dependencia:**
```xml
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-java-sdk-cloudwatchlogs</artifactId>
    <version>1.12.xxx</version>
</dependency>
```

2. **Modificar `LoggingConfig.java`:**
```java
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsClientBuilder;
import com.amazonaws.services.logs.model.PutLogEventsRequest;
import com.amazonaws.services.logs.model.InputLogEvent;

// En sendToExternalService:
AWSLogs logsClient = AWSLogsClientBuilder.defaultClient();
// Implementar envío a CloudWatch
```

### Opción 3: Google Cloud Logging

1. **Agregar dependencia:**
```xml
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>google-cloud-logging</artifactId>
</dependency>
```

2. **Modificar `LoggingConfig.java`:**
```java
import com.google.cloud.logging.Logging;
import com.google.cloud.logging.LoggingOptions;
import com.google.cloud.logging.LogEntry;
import com.google.cloud.logging.Severity;

// En sendToExternalService:
Logging logging = LoggingOptions.getDefaultInstance().getService();
// Implementar envío a Google Cloud Logging
```

### Opción 4: Datadog

1. **Agregar dependencia:**
```xml
<dependency>
    <groupId>com.datadoghq</groupId>
    <artifactId>java-dogstatsd-client</artifactId>
    <version>4.2.0</version>
</dependency>
```

2. **Modificar `LoggingConfig.java`:**
```java
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;

// En sendToExternalService:
StatsDClient statsd = new NonBlockingStatsDClient("miroma", "localhost", 8125);
statsd.incrementCounter("error.count", "level:" + entry.getLevel());
```

## Variables de Entorno Recomendadas

```bash
# Tipo de servicio de logging
LOGGING_SERVICE=elk|cloudwatch|gcp|datadog|local

# Configuración específica del servicio
LOGGING_ENDPOINT=https://your-logging-service.com
LOGGING_API_KEY=your-api-key
LOGGING_REGION=us-east-1
```

## Mejores Prácticas

1. **No loguear información sensible:**
   - Contraseñas
   - Tokens JWT completos
   - Datos personales sensibles
   - Números de tarjeta de crédito

2. **Niveles de log apropiados:**
   - ERROR: Errores que requieren atención
   - WARN: Situaciones anómalas pero manejables
   - INFO: Información importante del flujo
   - DEBUG: Información detallada (solo desarrollo)

3. **Retención de logs:**
   - Configurar políticas de retención según regulaciones
   - Considerar costos de almacenamiento
   - Implementar archivado para logs antiguos

4. **Monitoreo:**
   - Configurar alertas para errores críticos
   - Monitorear volumen de logs
   - Revisar patrones de error

## Próximos Pasos

1. Elegir el servicio de logging según infraestructura
2. Implementar el método `sendToExternalService` en `LoggingConfig.java`
3. Configurar variables de entorno
4. Probar la integración
5. Configurar alertas y dashboards

