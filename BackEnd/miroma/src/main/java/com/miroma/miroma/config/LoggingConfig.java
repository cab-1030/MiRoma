package com.miroma.miroma.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Configuración de logging para producción
 * En producción, esto debería integrarse con un servicio de logging externo
 * como ELK Stack, Splunk, CloudWatch, etc.
 */
@Configuration
@EnableScheduling
public class LoggingConfig {

    private static final Logger logger = LoggerFactory.getLogger(LoggingConfig.class);
    
    // Cola para almacenar logs antes de enviarlos
    private static final BlockingQueue<LogEntry> logQueue = new LinkedBlockingQueue<>();
    
    // Directorio de logs (en producción debería ser configurable)
    private static final String LOG_DIR = System.getProperty("user.home") + "/miroma-logs";
    
    /**
     * Clase interna para representar una entrada de log
     */
    public static class LogEntry {
        private final LocalDateTime timestamp;
        private final String level;
        private final String message;
        private final String context;
        private final Throwable exception;
        
        public LogEntry(String level, String message, String context, Throwable exception) {
            this.timestamp = LocalDateTime.now();
            this.level = level;
            this.message = message;
            this.context = context;
            this.exception = exception;
        }
        
        public String toJson() {
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"timestamp\":\"").append(timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\",");
            json.append("\"level\":\"").append(level).append("\",");
            json.append("\"message\":\"").append(escapeJson(message)).append("\",");
            json.append("\"context\":\"").append(escapeJson(context)).append("\"");
            
            if (exception != null) {
                json.append(",\"exception\":\"");
                json.append(escapeJson(exception.getClass().getName()));
                json.append("\",\"exceptionMessage\":\"");
                json.append(escapeJson(exception.getMessage())).append("\"");
            }
            
            json.append("}");
            return json.toString();
        }
        
        private String escapeJson(String str) {
            if (str == null) return "";
            return str.replace("\\", "\\\\")
                     .replace("\"", "\\\"")
                     .replace("\n", "\\n")
                     .replace("\r", "\\r")
                     .replace("\t", "\\t");
        }
    }
    
    /**
     * Método para agregar un log a la cola
     * En producción, esto podría enviar directamente a un servicio externo
     */
    public static void logToQueue(String level, String message, String context, Throwable exception) {
        try {
            logQueue.offer(new LogEntry(level, message, context, exception));
        } catch (Exception e) {
            logger.error("Error al agregar log a la cola", e);
        }
    }
    
    /**
     * Procesa los logs de la cola cada 30 segundos
     * En producción, esto podría enviar a un servicio de logging externo
     */
    @Scheduled(fixedRate = 30000) // Cada 30 segundos
    public void processLogQueue() {
        if (logQueue.isEmpty()) {
            return;
        }
        
        // Crear directorio de logs si no existe
        File logDir = new File(LOG_DIR);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
        
        // Archivo de log diario
        String logFileName = LOG_DIR + "/miroma-" + 
                           LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".log";
        
        try (FileWriter writer = new FileWriter(logFileName, true)) {
            int processed = 0;
            while (!logQueue.isEmpty() && processed < 100) { // Procesar hasta 100 logs por ciclo
                LogEntry entry = logQueue.poll();
                if (entry != null) {
                    writer.write(entry.toJson());
                    writer.write("\n");
                    processed++;
                }
            }
            writer.flush();
        } catch (IOException e) {
            logger.error("Error al escribir en archivo de log", e);
        }
    }
    
    /**
     * Método para enviar logs a un servicio externo (ejemplo con HTTP)
     * En producción, implementar según el servicio de logging elegido
     */
    public void sendToExternalService(LogEntry entry) {
        // Ejemplo: Enviar a un servicio de logging externo
        // En producción, esto podría ser:
        // - ELK Stack (Elasticsearch, Logstash, Kibana)
        // - Splunk
        // - AWS CloudWatch
        // - Google Cloud Logging
        // - Datadog
        // - etc.
        
        // Ejemplo básico con HTTP (comentado):
        /*
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer YOUR_API_KEY");
            
            HttpEntity<String> entity = new HttpEntity<>(entry.toJson(), headers);
            restTemplate.postForEntity("https://your-logging-service.com/api/logs", entity, String.class);
        } catch (Exception e) {
            logger.error("Error al enviar log a servicio externo", e);
        }
        */
    }
}

