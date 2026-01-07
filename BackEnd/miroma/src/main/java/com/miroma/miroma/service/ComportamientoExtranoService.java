package com.miroma.miroma.service;

import com.miroma.miroma.dto.ComportamientoExtranoResponse;
import com.miroma.miroma.entity.IntentoLogin;
import com.miroma.miroma.repository.IntentoLoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
public class ComportamientoExtranoService {

    @Autowired
    private IntentoLoginRepository intentoLoginRepository;

    /**
     * Obtiene todos los comportamientos extraños detectados
     * @return Lista de comportamientos extraños
     */
    public List<ComportamientoExtranoResponse> obtenerComportamientosExtranos() {
        List<IntentoLogin> intentos = intentoLoginRepository.findComportamientosExtranos();
        List<ComportamientoExtranoResponse> comportamientos = new ArrayList<>();

        for (IntentoLogin intento : intentos) {
            ComportamientoExtranoResponse comportamiento = mapearAComportamientoExtrano(intento);
            comportamientos.add(comportamiento);
        }

        return comportamientos;
    }

    /**
     * Mapea un IntentoLogin a ComportamientoExtranoResponse
     */
    private ComportamientoExtranoResponse mapearAComportamientoExtrano(IntentoLogin intento) {
        String tipoComportamiento = "Múltiples intentos de login fallidos";
        String descripcion = String.format(
            "El usuario con email %s ha intentado iniciar sesión fallidamente %d veces. " +
            "Nivel de bloqueo: %d. Último intento: %s",
            intento.getEmail(),
            intento.getIntentosFallidos(),
            intento.getNivelBloqueo(),
            formatearFecha(intento.getUltimoIntento())
        );

        if (intento.getBloqueadoHasta() != null) {
            Timestamp ahora = new Timestamp(System.currentTimeMillis());
            if (intento.getBloqueadoHasta().after(ahora)) {
                descripcion += String.format(" | Cuenta bloqueada hasta: %s", formatearFecha(intento.getBloqueadoHasta()));
            }
        }

        return new ComportamientoExtranoResponse(
            intento.getId(),
            intento.getEmail(),
            intento.getIntentosFallidos(),
            intento.getUltimoIntento(),
            intento.getBloqueadoHasta(),
            intento.getNivelBloqueo(),
            tipoComportamiento,
            descripcion
        );
    }

    /**
     * Formatea una fecha Timestamp a String legible
     */
    private String formatearFecha(Timestamp fecha) {
        if (fecha == null) {
            return "N/A";
        }
        return fecha.toString();
    }
}

