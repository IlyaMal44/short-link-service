package com.promoit.shortLink.domain.entity;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class LinkEntityTest {

    @Test
    @DisplayName("Ссылка должна быть доступна когда не истекла и лимит не достигнут")
    void canBeAccessed_WhenActiveAndUnderLimit_ShouldReturnTrue() {
        LinkEntity link = new LinkEntity(
                "test123", "https://example.com", new UserEntity(), 10, LocalDateTime.now().plusHours(24)
        );
        assertTrue(link.canBeAccessed());
        assertFalse(link.isExpired());
    }

    @Test
    @DisplayName("Ссылка должна быть недоступна когда истек срок")
    void canBeAccessed_WhenExpired_ShouldReturnFalse() {
        LinkEntity link = new LinkEntity(
                "test123", "https://example.com", new UserEntity(), 10, LocalDateTime.now().minusHours(1)
        );
        assertFalse(link.canBeAccessed());
        assertTrue(link.isExpired());
    }

    @Test
    @DisplayName("Ссылка должна быть недоступна когда достигнут лимит переходов")
    void canBeAccessed_WhenClickLimitReached_ShouldReturnFalse() {
        LinkEntity link = new LinkEntity(
                "test123", "https://example.com", new UserEntity(), 1, LocalDateTime.now().plusHours(24)
        );
        link.incrementClickCount();
        assertFalse(link.canBeAccessed());
        assertFalse(link.isExpired());
    }

    @Test
    @DisplayName("Ссылка с null лимитом должна быть всегда доступна (если не истекла)")
    void canBeAccessed_WithNullLimit_ShouldReturnTrue() {
        LinkEntity link = new LinkEntity(
                "test123", "https://example.com", new UserEntity(), null, LocalDateTime.now().plusHours(24)
        );
        link.incrementClickCount();
        link.incrementClickCount();
        assertTrue(link.canBeAccessed());
    }

    @Test
    @DisplayName("incrementClickCount должен увеличивать счетчик на 1")
    void incrementClickCount_ShouldIncreaseCountByOne() {
        LinkEntity link = new LinkEntity(
                "test123", "https://example.com", new UserEntity(), 10, LocalDateTime.now().plusHours(24)
        );
        assertEquals(0, link.getClickCount());
        link.incrementClickCount();
        assertEquals(1, link.getClickCount());
    }

    @Test
    @DisplayName("Ссылка должна быть создана с текущим временем создания")
    void constructor_ShouldSetCreatedAtToCurrentTime() {
        LinkEntity link = new LinkEntity(
                "test123", "https://example.com", new UserEntity(), 10, LocalDateTime.now().plusHours(24)
        );
        assertNotNull(link.getCreatedAt());
        assertTrue(link.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(link.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(1)));
    }
}