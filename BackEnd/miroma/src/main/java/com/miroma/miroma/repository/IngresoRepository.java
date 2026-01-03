package com.miroma.miroma.repository;

import com.miroma.miroma.entity.Ingreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public interface IngresoRepository extends JpaRepository<Ingreso, Integer> {
    
    // Buscar ingresos por usuario_id
    List<Ingreso> findByUsuarioIdOrderByFechaDesc(Integer usuarioId);
    
    // Buscar ingresos por pareja_id
    List<Ingreso> findByParejaIdOrderByFechaDesc(Integer parejaId);
    
    // Buscar ingresos de un usuario o su pareja
    @Query("SELECT i FROM Ingreso i WHERE i.usuarioId = :userId OR i.parejaId = :parejaId ORDER BY i.fecha DESC")
    List<Ingreso> findByUsuarioOrPareja(@Param("userId") Integer userId, @Param("parejaId") Integer parejaId);
    
    // Buscar ingresos por rango de fechas
    @Query("SELECT i FROM Ingreso i WHERE (i.usuarioId = :userId OR i.parejaId = :parejaId) AND i.fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY i.fecha DESC")
    List<Ingreso> findByUsuarioAndFechaBetween(@Param("userId") Integer userId, @Param("parejaId") Integer parejaId, @Param("fechaInicio") Date fechaInicio, @Param("fechaFin") Date fechaFin);
}

