package com.miroma.miroma.config;

import org.springframework.context.annotation.Configuration;

/**
 * WebConfig - Ya no es necesario registrar el filtro JWT manualmente
 * Spring Security ahora maneja el filtro JWT a través de SecurityConfig
 */
@Configuration
public class WebConfig {
    // El filtro JWT ahora se maneja a través de Spring Security en SecurityConfig
}

