package com.miroma.miroma.repository;

import com.miroma.miroma.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    
    Optional<RefreshToken> findByToken(String token);
    
    Optional<RefreshToken> findByUsuarioId(Integer usuarioId);
    
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.usuarioId = :usuarioId")
    void deleteByUsuarioId(Integer usuarioId);
    
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(Timestamp now);
    
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.usuarioId = :usuarioId")
    void revokeByUsuarioId(Integer usuarioId);
}

