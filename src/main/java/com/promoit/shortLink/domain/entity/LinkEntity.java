package com.promoit.shortLink.domain.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class LinkEntity {
    @Id
    private String shortCode;
    @Column(nullable = false, length = 2048)
    private String originalUrl;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
    private Integer clickLimit;
    private Integer clickCount = 0;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    public LinkEntity(String shortCode, String originalUrl, UserEntity user, Integer clickLimit, LocalDateTime expiresAt) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.user = user;
        this.clickLimit = clickLimit;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
    }

    /**
     * Проверяет, истек ли срок действия ссылки.
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Проверяет, доступна ли ссылка для перехода.
     */
    public boolean canBeAccessed() {
        return !isExpired() && (clickLimit == null || clickCount < clickLimit);
    }

    /**
     * Увеличивает счетчик переходов по ссылке на 1.
     */
    public void incrementClickCount() {
        this.clickCount++;
    }
}