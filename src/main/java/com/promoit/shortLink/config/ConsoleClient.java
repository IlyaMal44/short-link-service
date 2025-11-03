package com.promoit.shortLink.config;

import com.promoit.shortLink.domain.entity.LinkEntity;
import com.promoit.shortLink.domain.entity.UserEntity;
import com.promoit.shortLink.service.LinkService;
import com.promoit.shortLink.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.awt.*;
import java.util.List;
import java.net.URI;
import java.util.Scanner;


@Profile("!test")
@Component
public class ConsoleClient implements CommandLineRunner {
    @Autowired
    private LinkService linkService;
    @Autowired
    private UserService userService;

    private final Scanner scanner = new Scanner(System.in);

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Сервис сокращения ссылок запущен!");
        Thread.sleep(1000);  // Ждем завершения стартовых логов
        System.out.println();      // Разделитель
        showMenu();
        while (true) {
            System.out.print("Выберите действие (1-4): ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> createShortLink();
                case "2" -> openShortLinkInBrowser();
                case "3" -> showUserLinks();
                case "4" -> {
                    System.out.println("Выход из программы...");
                    return;
                }
                default -> {
                    System.out.println("Неверный выбор. Попробуйте снова.");
                    showMenu();
                }
            }
        }
    }

    private void showMenu() {
        System.out.println("Меню:");
        System.out.println("1. Создать короткую ссылку");
        System.out.println("2. Перейти по короткой ссылке в браузере");
        System.out.println("3. Показать мои ссылки");
        System.out.println("4. Выход");
    }

    private void createShortLink() {
        try {
            System.out.print("Введите длинный URL: ");
            String url = scanner.nextLine().trim();

            if (url.isEmpty()) {
                System.out.println("URL не может быть пустым");
                return;
            }

            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                System.out.println("URL должен начинаться с http:// или https://");
                return;
            }

            System.out.print("Лимит переходов (Enter для бесконечного): ");
            String limitInput = scanner.nextLine().trim();
            Integer clickLimit = limitInput.isEmpty() ? null : Integer.parseInt(limitInput);

            if (clickLimit != null && clickLimit <= 0) {
                System.out.println("Лимит должен быть положительным числом");
                return;
            }

            System.out.print("Ваш User ID (Enter для автоматического создания): ");
            String userId = scanner.nextLine().trim();
            userId = userId.isEmpty() ? null : userId;

            UserEntity user = userService.getOrCreateUser(userId);
            LinkEntity link = linkService.createShortLink(url, user, clickLimit);

            String result = String.format(
                    "Создана короткая ссылка:\n" +
                            "Короткая: %s\n" +
                            "Код: %s\n" +
                            "User ID: %s\n" +
                            "Истекает: %s\n" +
                            "Лимит: %s",
                    linkService.buildShortUrl(link.getShortCode()),
                    link.getShortCode(),
                    user.getId(),
                    link.getExpiresAt(),
                    link.getClickLimit() == null ? "бесконечно" : link.getClickLimit()
            );
            System.out.println(result);
        } catch (NumberFormatException e) {
            System.out.println("Лимит должен быть числом");
        } catch (Exception e) {
            System.out.println("Ошибка при создании ссылки: " + e.getMessage());
        }
    }


    private void openShortLinkInBrowser() {
        try {
            System.out.print("Введите короткий код ссылки: ");
            String shortCode = scanner.nextLine().trim();

            if (shortCode.isEmpty()) {
                System.out.println("Код ссылки не может быть пустым");
                return;
            }

            String shortUrl = linkService.buildShortUrl(shortCode);
            System.out.println("Открываю: " + shortUrl);

            var linkOpt = linkService.accessLink(shortCode);
            if (linkOpt.isEmpty()) {
                System.out.println("Ссылка недоступна (истекла или достигнут лимит)");
                return;
            }

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(shortUrl));
                System.out.println("Ссылка открыта в браузере!");
            } else {
                System.out.println("Не удалось открыть браузер автоматически");
                System.out.println("Скопируйте ссылку вручную: " + shortUrl);
            }

        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void showUserLinks() {
        try {
            System.out.print("Введите ваш User ID: ");
            String userId = scanner.nextLine().trim();

            if (userId.isEmpty()) {
                System.out.println("User ID не может быть пустым");
                return;
            }

            var userOpt = userService.getUserById(userId);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь с ID '" + userId + "' не найден");
                return;
            }

            List<LinkEntity> links = linkService.getUserLinks(userId);

            if (links.isEmpty()) {
                System.out.println("У вас пока нет созданных ссылок");
            } else {
                System.out.println("Ваши ссылки:");
                for (int i = 0; i < links.size(); i++) {
                    LinkEntity link = links.get(i);
                    String status = link.canBeAccessed() ? "АКТИВНА" : "НЕДОСТУПНА";
                    String clicks = link.getClickCount() + "/" + (link.getClickLimit() == null ? "∞" : link.getClickLimit());
                    System.out.println((i + 1) + ". " + link.getShortCode() + " - " + status);
                    System.out.println("URL: " + truncateUrl(link.getOriginalUrl(), 50));
                    System.out.println("Переходы: " + clicks + ", Истекает: " + link.getExpiresAt().toLocalDate());
                }
            }
        } catch (Exception e) {
            System.out.println("Ошибка при получении ссылок: " + e.getMessage());
        }
    }

    private String truncateUrl(String url, int maxLength) {
        if (url.length() <= maxLength) {
            return url;
        }
        return url.substring(0, maxLength - 3) + "...";
    }

}