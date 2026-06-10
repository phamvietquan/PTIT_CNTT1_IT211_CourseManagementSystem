package com.example.coursemanagementsystem.repository;

import com.example.coursemanagementsystem.model.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {

    boolean existsByToken(String token);

    void deleteByExpiryDateBefore(LocalDateTime dateTime);
}
