package com.miroma.miroma.repository;

import com.miroma.miroma.entity.Presupuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PresupuestoRepository extends JpaRepository<Presupuesto, Integer> {
    
    // Buscar presupuestos por pareja_id
    List<Presupuesto> findByParejaIdOrderByFechaCreacionDesc(Integer parejaId);
    
    // Buscar presupuesto por pareja_id y periodo
    @Query("SELECT p FROM Presupuesto p WHERE p.parejaId = :parejaId AND p.periodo = :periodo")
    Optional<Presupuesto> findByParejaIdAndPeriodo(@Param("parejaId") Integer parejaId, @Param("periodo") String periodo);
    
    // Verificar si existe un presupuesto para una pareja y periodo
    @Query("SELECT COUNT(p) > 0 FROM Presupuesto p WHERE p.parejaId = :parejaId AND p.periodo = :periodo")
    boolean existsByParejaIdAndPeriodo(@Param("parejaId") Integer parejaId, @Param("periodo") String periodo);
}

