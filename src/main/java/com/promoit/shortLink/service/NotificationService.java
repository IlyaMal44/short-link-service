package com.promoit.shortLink.service;

import com.promoit.shortLink.domain.entity.LinkEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {
    /**
     * Уведомляет пользователя о недоступности его ссылки
     * @param link ссылка которая стала недоступна
     * @param reason причина недоступности
     */
    public void notifyLinkUnavailable(LinkEntity link, String reason) {
        String message = String.format("Your short link %s is no longer available. Reason: %s", link.getShortCode(), reason);
        log.info("User notification: {} - User: {}", message, link.getUser().getId());
    }
}
