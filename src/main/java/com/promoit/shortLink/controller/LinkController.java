package com.promoit.shortLink.controller;

import com.promoit.shortLink.domain.entity.LinkEntity;
import com.promoit.shortLink.domain.entity.UserEntity;
import com.promoit.shortLink.service.LinkService;
import com.promoit.shortLink.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
public class LinkController {
    @Autowired
    private LinkService linkService;
    @Autowired
    private UserService userService;

    /**
     * Создает новую короткую ссылку для указанного URL.
     * @param url        оригинальный URL для сокращения
     * @param clickLimit максимальное количество переходов (опционально)
     * @param userId     идентификатор пользователя из заголовка X-User-ID (опционально)
     * @return ResponseEntity с данными созданной короткой ссылки
     */
    @PostMapping("/shorten")
    public ResponseEntity<?> createShortLink(
            @RequestParam String url,
            @RequestParam(required = false) Integer clickLimit,
            @RequestHeader(value = "X-User-ID", required = false) String userId) {
        try {
            UserEntity user = userService.getOrCreateUser(userId);
            LinkEntity link = linkService.createShortLink(url, user, clickLimit);

            Map<String, Object> response = new HashMap<>();
            response.put("shortUrl", linkService.buildShortUrl(link.getShortCode()));
            response.put("shortCode", link.getShortCode());
            response.put("userId", user.getId());
            response.put("expiresAt", link.getExpiresAt());
            response.put("clickLimit", link.getClickLimit());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating short link: " + e.getMessage());
        }
    }

    /**
     * Выполняет редирект по короткой ссылке на оригинальный URL, а так же проверяет лимиты переходов и срок действия ссылки.
     * @param shortCode уникальный код короткой ссылки из URL
     * @param response  объект HTTP ответа для ручного управления редиректом
     * @throws IOException если произошла ошибка ввода-вывода при отправке ответа
     */
    @GetMapping("/{shortCode}")
    public void redirectToOriginal(@PathVariable String shortCode, HttpServletResponse response) throws IOException {
        Optional<LinkEntity> linkOpt = linkService.accessLink(shortCode);
        if (linkOpt.isPresent()) {
            LinkEntity link = linkOpt.get();
            response.sendRedirect(link.getOriginalUrl());
        } else {
            response.setStatus(HttpServletResponse.SC_GONE);
            response.getWriter().write("Link is expired or reached click limit");
        }
    }

    /**
     * Удаляет короткую ссылку пользователя.Доступно только для создателя ссылки.
     * @param shortCode уникальный код короткой ссылки
     * @param userId    идентификатор пользователя из заголовка X-User-ID
     * @return ResponseEntity с сообщением об успешном удалении или ошибке
     */
    @DeleteMapping("/{shortCode}")
    public ResponseEntity<?> deleteLink(@PathVariable String shortCode, @RequestHeader("X-User-ID") String userId) {
        if (linkService.deleteLink(shortCode, userId)) {
            return ResponseEntity.ok("Link deleted successfully");
        } else {
            return ResponseEntity.badRequest().body("Link not found or access denied");
        }
    }

    /**
     * Обновляет максимальное количество переходов по короткой ссылке. Доступно только для создателя ссылки.
     * @param shortCode уникальный код короткой ссылки (path variable)
     * @param newLimit  новое значение лимита переходов (query parameter, обязательный)
     * @param userId    идентификатор пользователя из заголовка X-User-ID (обязательный)
     * @return ResponseEntity с обновленным объектом ссылки или сообщением об ошибке
     */
    @PutMapping("/{shortCode}/limit")
    public ResponseEntity<?> updateClickLimit(
            @PathVariable String shortCode,
            @RequestParam Integer newLimit,
            @RequestHeader("X-User-ID") String userId) {
        try {
            LinkEntity updatedLink = linkService.updateClickLimit(shortCode, userId, newLimit);
            return ResponseEntity.ok(updatedLink);
        } catch (SecurityException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Возвращает все короткие ссылки, принадлежащие указанному пользователю.
     * @param userId идентификатор пользователя из заголовка X-User-ID (обязательный)
     * @return ResponseEntity со списком ссылок пользователя или сообщением об ошибке
     */
    @GetMapping("/user/links")
    public ResponseEntity<?> getUserLinks(
            @RequestHeader("X-User-ID") String userId) {
        if (!userService.getUserById(userId).isPresent()) {
            return ResponseEntity.badRequest().body("User not found");
        }
        List<LinkEntity> links = linkService.getUserLinks(userId);
        return ResponseEntity.ok(links);
    }

}