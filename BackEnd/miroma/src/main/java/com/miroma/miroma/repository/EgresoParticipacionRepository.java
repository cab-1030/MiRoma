package com.miroma.miroma.repository;

import com.miroma.miroma.entity.EgresoParticipacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EgresoParticipacionRepository extends JpaRepository<EgresoParticipacion, Integer> {
    
    // Buscar participaciones por egreso_id
    List<EgresoParticipacion> findByEgresoId(Integer egresoId);
    
    // Buscar participaciones por usuario_id
    List<EgresoParticipacion> findByUsuarioId(Integer usuarioId);
    
    // Eliminar todas las participaciones de un egreso
    void deleteByEgresoId(Integer egresoId);
}

