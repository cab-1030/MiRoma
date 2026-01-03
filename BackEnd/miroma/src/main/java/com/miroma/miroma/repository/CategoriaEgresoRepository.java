package com.miroma.miroma.repository;

import com.miroma.miroma.entity.CategoriaEgreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaEgresoRepository extends JpaRepository<CategoriaEgreso, Integer> {
    
    List<CategoriaEgreso> findAllByOrderByNombreAsc();
    
    Optional<CategoriaEgreso> findByNombreIgnoreCase(String nombre);
    
    boolean existsByNombreIgnoreCase(String nombre);
}

