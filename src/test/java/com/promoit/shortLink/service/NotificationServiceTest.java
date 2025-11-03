package com.promoit.shortLink.service;

import com.promoit.shortLink.domain.entity.LinkEntity;
import com.promoit.shortLink.domain.entity.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private final NotificationService notificationService = new NotificationService();

    @Test
    @DisplayName("Уведомление о недоступности ссылки должно выполняться без ошибок")
    void notifyLinkUnavailable_ShouldLogMessage() {
        UserEntity user = new UserEntity();
        LinkEntity expiredLink = new LinkEntity(
                "test123", "https://example.com", user, 10, LocalDateTime.now().minusHours(1)
        );
        assertDoesNotThrow(() -> notificationService.notifyLinkUnavailable(expiredLink, "test reason"));
    }
}