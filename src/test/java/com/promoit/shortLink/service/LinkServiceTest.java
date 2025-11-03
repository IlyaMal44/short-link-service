package com.promoit.shortLink.service;

import com.promoit.shortLink.config.AppConfig;
import com.promoit.shortLink.domain.entity.LinkEntity;
import com.promoit.shortLink.domain.entity.UserEntity;
import com.promoit.shortLink.domain.repository.LinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LinkServiceTest {
    @Mock
    private LinkRepository linkRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private AppConfig appConfig;
    @InjectMocks
    private LinkService linkService;
    private UserEntity testUser;
    private LinkEntity activeLink;
    private LinkEntity expiredLink;
    private LinkEntity limitReachedLink;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        activeLink = new LinkEntity(
                "active123", "https://active.com", testUser, 10, LocalDateTime.now().plusHours(24)
        );
        expiredLink = new LinkEntity(
                "expired123", "https://expired.com", testUser, 10, LocalDateTime.now().minusHours(1)
        );
        limitReachedLink = new LinkEntity(
                "limit123", "https://limit.com", testUser, 1, LocalDateTime.now().plusHours(24)
        );
        limitReachedLink.incrementClickCount();
    }

    @Test
    @DisplayName("Создание короткой ссылки должно генерировать уникальный код и устанавливать срок действия")
    void createShortLink_ShouldCreateLinkWithUniqueCode() {
        when(appConfig.getCodeLength()).thenReturn(9);
        when(appConfig.getDefaultTtlHours()).thenReturn(24);
        when(linkRepository.save(any(LinkEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        LinkEntity result = linkService.createShortLink("https://example.com", testUser, 100);

        assertNotNull(result);
        assertNotNull(result.getShortCode());
        assertEquals(9, result.getShortCode().length());
        assertEquals("https://example.com", result.getOriginalUrl());
        assertEquals(testUser, result.getUser());
        assertEquals(100, result.getClickLimit());
        assertTrue(result.getExpiresAt().isAfter(LocalDateTime.now()));
        verify(linkRepository).save(any(LinkEntity.class));
    }

    @Test
    @DisplayName("Создание ссылки с null лимитом должно создавать бессрочную ссылку")
    void createShortLink_WithNullLimit_ShouldCreateLinkWithoutLimit() {
        when(linkRepository.save(any(LinkEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        LinkEntity result = linkService.createShortLink("https://example.com", testUser, null);
        assertNull(result.getClickLimit());
    }

    @Test
    @DisplayName("Доступ к активной ссылке должен увеличивать счетчик переходов")
    void accessLink_WithActiveLink_ShouldIncrementClickCount() {
        when(linkRepository.findByShortCode("active123")).thenReturn(Optional.of(activeLink));
        when(linkRepository.save(any(LinkEntity.class))).thenReturn(activeLink);

        Optional<LinkEntity> result = linkService.accessLink("active123");

        assertTrue(result.isPresent());
        assertEquals(1, activeLink.getClickCount());
        verify(linkRepository).save(activeLink);
    }

    @Test
    @DisplayName("Доступ к просроченной ссылке должен возвращать empty и отправлять уведомление")
    void accessLink_WithExpiredLink_ShouldReturnEmpty() {
        when(linkRepository.findByShortCode("expired123")).thenReturn(Optional.of(expiredLink));

        Optional<LinkEntity> result = linkService.accessLink("expired123");

        assertTrue(result.isEmpty());
        verify(notificationService).notifyLinkUnavailable(expiredLink, "Link is no longer available");
        verify(linkRepository, never()).save(any());
    }

    @Test
    @DisplayName("Доступ к ссылке с достигнутым лимитом должен возвращать empty и отправлять уведомление")
    void accessLink_WithLimitReachedLink_ShouldReturnEmpty() {
        when(linkRepository.findByShortCode("limit123")).thenReturn(Optional.of(limitReachedLink));

        Optional<LinkEntity> result = linkService.accessLink("limit123");

        assertTrue(result.isEmpty());
        verify(notificationService).notifyLinkUnavailable(limitReachedLink, "Link is no longer available");
    }

    @Test
    @DisplayName("Доступ к несуществующей ссылке должен возвращать empty")
    void accessLink_WithNonExistentCode_ShouldReturnEmpty() {
        when(linkRepository.findByShortCode("nonexistent")).thenReturn(Optional.empty());

        Optional<LinkEntity> result = linkService.accessLink("nonexistent");

        assertTrue(result.isEmpty());
        verify(notificationService, never()).notifyLinkUnavailable(any(), any());
    }

    @Test
    @DisplayName("Удаление ссылки владельцем должно возвращать true и удалять ссылку")
    void deleteLink_WithValidOwner_ShouldReturnTrue() {
        String userId = testUser.getId();
        when(linkRepository.findByShortCode("active123")).thenReturn(Optional.of(activeLink));

        boolean result = linkService.deleteLink("active123", userId);

        assertTrue(result);
        verify(linkRepository).delete(activeLink);
    }
}