package com.miroma.miroma.repository;

import com.miroma.miroma.entity.LogEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface LogEventoRepository extends JpaRepository<LogEvento, Integer> {
    
    // Buscar eventos por usuario_id ordenados por fecha descendente
    List<LogEvento> findByUsuarioIdOrderByFechaDesc(Integer usuarioId);
    
    // Buscar eventos por usuario_id y rango de fechas
    @Query("SELECT e FROM LogEvento e WHERE e.usuarioId = :usuarioId AND e.fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY e.fecha DESC")
    List<LogEvento> findByUsuarioIdAndFechaBetween(@Param("usuarioId") Integer usuarioId, 
                                                     @Param("fechaInicio") Timestamp fechaInicio, 
                                                     @Param("fechaFin") Timestamp fechaFin);
    
    // Buscar eventos por usuario_id y acción que contenga texto
    @Query("SELECT e FROM LogEvento e WHERE e.usuarioId = :usuarioId AND e.accion LIKE CONCAT('%', :texto, '%') ORDER BY e.fecha DESC")
    List<LogEvento> findByUsuarioIdAndAccionContaining(@Param("usuarioId") Integer usuarioId, 
                                                        @Param("texto") String texto);
    
    // Buscar eventos por usuario_id, rango de fechas y acción que contenga texto
    @Query("SELECT e FROM LogEvento e WHERE e.usuarioId = :usuarioId AND e.fecha BETWEEN :fechaInicio AND :fechaFin AND e.accion LIKE CONCAT('%', :texto, '%') ORDER BY e.fecha DESC")
    List<LogEvento> findByUsuarioIdAndFechaBetweenAndAccionContaining(@Param("usuarioId") Integer usuarioId,
                                                                       @Param("fechaInicio") Timestamp fechaInicio,
                                                                       @Param("fechaFin") Timestamp fechaFin,
                                                                       @Param("texto") String texto);
}

