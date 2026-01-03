package com.miroma.miroma.repository;

import com.miroma.miroma.entity.PresupuestoCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PresupuestoCategoriaRepository extends JpaRepository<PresupuestoCategoria, Integer> {
    
    // Buscar todas las categorías de un presupuesto
    List<PresupuestoCategoria> findByPresupuestoId(Integer presupuestoId);
    
    // Buscar por presupuesto y categoría
    Optional<PresupuestoCategoria> findByPresupuestoIdAndCategoriaId(Integer presupuestoId, Integer categoriaId);
    
    // Verificar si existe una categoría en un presupuesto
    boolean existsByPresupuestoIdAndCategoriaId(Integer presupuestoId, Integer categoriaId);
    
    // Sumar todos los porcentajes de un presupuesto
    @Query("SELECT COALESCE(SUM(pc.porcentaje), 0) FROM PresupuestoCategoria pc WHERE pc.presupuestoId = :presupuestoId")
    BigDecimal sumarPorcentajesPorPresupuesto(@Param("presupuestoId") Integer presupuestoId);
    
    // Sumar todos los porcentajes de un presupuesto excluyendo un registro específico (útil para edición)
    @Query("SELECT COALESCE(SUM(pc.porcentaje), 0) FROM PresupuestoCategoria pc WHERE pc.presupuestoId = :presupuestoId AND pc.id != :excludeId")
    BigDecimal sumarPorcentajesPorPresupuestoExcluyendo(@Param("presupuestoId") Integer presupuestoId, @Param("excludeId") Integer excludeId);
}

