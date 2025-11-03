package com.promoit.shortLink.domain.repository;

import com.promoit.shortLink.domain.entity.LinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LinkRepository extends JpaRepository<LinkEntity, String> {

    Optional<LinkEntity> findByShortCode(String shortCode);

    List<LinkEntity> findByUserId(String userId);

    @Transactional
    @Modifying
    @Query("DELETE FROM LinkEntity l WHERE l.expiresAt < :now")
    int deleteExpiredLinks(@Param("now") LocalDateTime now);

    @Query("SELECT l FROM LinkEntity l WHERE l.expiresAt < :now")
    List<LinkEntity> findByExpiresAtBefore(@Param("now") LocalDateTime now);
}