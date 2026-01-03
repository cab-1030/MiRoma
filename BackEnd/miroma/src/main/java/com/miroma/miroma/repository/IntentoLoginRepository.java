package com.miroma.miroma.repository;

import com.miroma.miroma.entity.IntentoLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IntentoLoginRepository extends JpaRepository<IntentoLogin, Integer> {
    Optional<IntentoLogin> findByEmail(String email);
}

