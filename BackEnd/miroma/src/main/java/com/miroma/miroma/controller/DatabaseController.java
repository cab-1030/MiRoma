package com.miroma.miroma.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DatabaseController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/db/test")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            response.put("status", "success");
            response.put("message", "Conexión a la base de datos establecida correctamente");
            response.put("database", metaData.getDatabaseProductName());
            response.put("version", metaData.getDatabaseProductVersion());
            response.put("driver", metaData.getDriverName());
            response.put("driverVersion", metaData.getDriverVersion());
            response.put("url", metaData.getURL());
            response.put("username", metaData.getUserName());
            response.put("readOnly", connection.isReadOnly());
            response.put("catalog", connection.getCatalog());
            
            return ResponseEntity.ok(response);
        } catch (SQLException e) {
            response.put("status", "error");
            response.put("message", "Error al conectar con la base de datos: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}

