package com.promoit.shortLink.service;

import com.promoit.shortLink.config.AppConfig;
import com.promoit.shortLink.domain.entity.LinkEntity;
import com.promoit.shortLink.domain.entity.UserEntity;
import com.promoit.shortLink.domain.repository.LinkRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import static com.promoit.shortLink.utils.StringUtils.truncateUrl;

@Slf4j
@Service
public class LinkService {
    @Autowired
    private LinkRepository linkRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    AppConfig appConfig;

    /**
     * Создает новую короткую ссылку для указанного URL, а также генерирует уникальный код и устанавливает срок действия.
     * @param originalUrl оригинальный URL для сокращения
     * @param user пользователь, создающий ссылку
     * @param clickLimit лимит переходов (может быть null для бессрочного использования)
     * @return созданная сущность короткой ссылки
     */
    public LinkEntity createShortLink(String originalUrl, UserEntity user, Integer clickLimit) {
        String shortCode = RandomStringUtils.secure().nextAlphanumeric(appConfig.getCodeLength());
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(appConfig.getDefaultTtlHours());
        LinkEntity link = new LinkEntity(shortCode, originalUrl, user, clickLimit, expiresAt);
        return linkRepository.save(link);
    }

    /**
     * Строит полный URL для короткой ссылки
     * @param shortCode код короткой ссылки
     * @return полный URL
     */
    public String buildShortUrl(String shortCode) {
        return appConfig.getBaseUrl() + "/" + shortCode;
    }

    /**
     * Проверяет доступность ссылки и увеличивает счетчик переходов при успешном доступе.
     * @param shortCode код короткой ссылки
     * @return Optional с ссылкой если она доступна, иначе empty
     */
    @Transactional
    public Optional<LinkEntity> accessLink(String shortCode) {
        Optional<LinkEntity> linkOpt = linkRepository.findByShortCode(shortCode);
        if (linkOpt.isPresent()) {
            LinkEntity link = linkOpt.get();
            if (link.canBeAccessed()) {
                link.incrementClickCount();
                linkRepository.save(link);
                return Optional.of(link);
            } else {
                notificationService.notifyLinkUnavailable(link, "Link is no longer available");
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    /**
     * Удаляет короткую ссылку если пользователь является её владельцем.
     * @param shortCode код удаляемой ссылки
     * @param userId идентификатор пользователя для проверки прав
     * @return true если удаление успешно, false если нет прав или ссылка не найдена
     */
    @Transactional
    public boolean deleteLink(String shortCode, String userId) {
        Optional<LinkEntity> linkOpt = linkRepository.findByShortCode(shortCode);
        if (linkOpt.isPresent() && linkOpt.get().getUser().getId().equals(userId)) {
            linkRepository.delete(linkOpt.get());
            return true;
        }
        return false;
    }

    /**
     * Автоматическая очистка просроченных ссылок по расписанию.
     * Удаляет ссылки с истекшим TTL, логирует операции и уведомляет пользователей.
     * Интервал очистки настраивается в app.cleanup.interval (по умолчанию 1 час).
     */
    @Transactional
    @Scheduled(fixedRateString = "#{@appConfig.cleanupInterval}")
    public void scheduledDeleteExpiredLinks() {
        log.info("Starting the removal of expired links on a schedule");
        LocalDateTime now = LocalDateTime.now();

        List<LinkEntity> expiredLinks = linkRepository.findByExpiresAtBefore(now);

        if (expiredLinks.isEmpty()) {
            log.info("CLEANUP - No expired links found");
            return;
        }

        for (LinkEntity link : expiredLinks) {
            log.info("CLEANUP - Removing link: {} (User: {}, Expired: {}, Original: {})",
                    link.getShortCode(),
                    link.getUser().getId(),
                    link.getExpiresAt(),
                    truncateUrl(link.getOriginalUrl(), 50));

            notificationService.notifyLinkUnavailable(link, "Link expired automatically");
        }
        linkRepository.deleteAll(expiredLinks);
        log.info("CLEANUP - Completed! Removed {} expired links", expiredLinks.size());
    }

    /**
     * Обновляет лимит переходов для существующей ссылки, если пользователь является её владельцем.
     * @param shortCode код ссылки для обновления
     * @param userId идентификатор пользователя для проверки прав
     * @param newClickLimit новое значение лимита переходов
     * @return обновленная сущность ссылки
     * @throws SecurityException если пользователь не является владельцем ссылки
     */
    @Transactional
    public LinkEntity updateClickLimit(String shortCode, String userId, Integer newClickLimit) {
        Optional<LinkEntity> linkOpt = linkRepository.findByShortCode(shortCode);
        if (linkOpt.isPresent() && linkOpt.get().getUser().getId().equals(userId)) {
            LinkEntity link = linkOpt.get();
            link.setClickLimit(newClickLimit);
            return linkRepository.save(link);
        }
        throw new SecurityException("Link not found or access denied");
    }

    /**
     * Возвращает все ссылки, принадлежащие указанному пользователю.
     * @param userId идентификатор пользователя
     * @return список ссылок пользователя (может быть пустым)
     */
    public List<LinkEntity> getUserLinks(String userId) {
        return linkRepository.findByUserId(userId);
    }

}