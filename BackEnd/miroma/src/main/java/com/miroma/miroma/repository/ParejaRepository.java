package com.miroma.miroma.repository;

import com.miroma.miroma.entity.Pareja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParejaRepository extends JpaRepository<Pareja, Integer> {
    
    // Buscar pareja por esposo_id
    Optional<Pareja> findByEsposoId(Integer esposoId);
    
    // Buscar pareja por esposa_id
    Optional<Pareja> findByEsposaId(Integer esposaId);
    
    // Buscar pareja donde uno de los usuarios sea esposo o esposa
    @Query("SELECT p FROM Pareja p WHERE p.esposoId = :userId OR p.esposaId = :userId")
    Optional<Pareja> findByUsuarioId(Integer userId);
}

