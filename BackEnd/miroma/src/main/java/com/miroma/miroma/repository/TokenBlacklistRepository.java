package com.miroma.miroma.repository;

import com.miroma.miroma.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Optional;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Integer> {
    
    Optional<TokenBlacklist> findByTokenHash(String tokenHash);
    
    boolean existsByTokenHash(String tokenHash);
    
    @Modifying
    @Query("DELETE FROM TokenBlacklist t WHERE t.fechaExpiracion < :now")
    void deleteExpiredTokens(@Param("now") Timestamp now);
    
    @Modifying
    @Query("DELETE FROM TokenBlacklist t WHERE t.usuarioId = :usuarioId")
    void deleteAllByUsuarioId(@Param("usuarioId") Integer usuarioId);
}


