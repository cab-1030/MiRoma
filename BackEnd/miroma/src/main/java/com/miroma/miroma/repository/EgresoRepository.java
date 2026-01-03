package com.miroma.miroma.repository;

import com.miroma.miroma.entity.Egreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public interface EgresoRepository extends JpaRepository<Egreso, Integer> {
    
    // Buscar egresos por pareja_id
    List<Egreso> findByParejaIdOrderByFechaDesc(Integer parejaId);
    
    // Buscar egresos por categoria_id
    List<Egreso> findByCategoriaIdOrderByFechaDesc(Integer categoriaId);
    
    // Buscar egresos por pareja_id y rango de fechas
    @Query("SELECT e FROM Egreso e WHERE e.parejaId = :parejaId AND e.fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY e.fecha DESC")
    List<Egreso> findByParejaIdAndFechaBetween(@Param("parejaId") Integer parejaId, @Param("fechaInicio") Date fechaInicio, @Param("fechaFin") Date fechaFin);
}

