package com.miroma.miroma.repository;

import com.miroma.miroma.entity.IntentoLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntentoLoginRepository extends JpaRepository<IntentoLogin, Integer> {
    Optional<IntentoLogin> findByEmail(String email);
    
    /**
     * Obtiene todos los intentos de login con más de 6 intentos fallidos
     */
    @Query("SELECT i FROM IntentoLogin i WHERE i.intentosFallidos > 6 ORDER BY i.intentosFallidos DESC, i.ultimoIntento DESC")
    List<IntentoLogin> findComportamientosExtranos();
}

