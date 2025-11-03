# ShortLink Service
Сервис сокращения ссылок с ограничением переходов и временем жизни.

## Возможности
- Создание коротких ссылок
- Ограничение количества переходов
- Автоматическое удаление просроченных ссылок
- Уникальные ссылки для каждого пользователя
- REST API + CLI интерфейс

##  Технологии
- Java 17
- Spring Boot 3.x
- H2 Database
- JPA/Hibernate
- Gradle
- JUnit 5 + Mockito

### Запуск приложения:
# Через IDE (IntelliJ):
1) Откройте ShortLinkApplication.java
2) Нажмите Run → Будет работать и REST API и CLI меню
# Для CLI интерфейса:
1) Сначала соберите JAR  ./gradlew build
2) Затем запустите напрямую  java -jar build/libs/shortLink-0.0.1-SNAPSHOT.jar
# Запуск тестов  
1) /gradlew test
2) Нажмите Run на пакете test → Произойдет запуск всех тестов


### Требования
- Java 17+
- Gradle

##  Архитектура
src/
├── main/
│   ├── java/com/promoit/shortLink/
│   │   ├── config/
│   │   │   ├── AppConfig.java        # Конфигурация параметров
│   │   │   └── ConsoleClient.java    # CLI интерфейс
│   │   ├── controller/
│   │   │   └── LinkController.java   # REST endpoints
│   │   ├── domain/
│   │   │   ├── entity/
│   │   │   │   ├── LinkEntity.java   # Сущность ссылки
│   │   │   │   └── UserEntity.java   # Сущность пользователя
│   │   │   └── repository/
│   │   │       ├── LinkRepository.java
│   │   │       └── UserRepository.java
│   │   └── service/
│   │       ├── LinkService.java           # Основная бизнес-логика
│   │       ├── UserService.java           # Управление пользователями
│   │       └── NotificationService.java   # Уведомления
│   └── resources/
│       └── application.yml           # Конфигурация
└── test/                             # Тесты


### API Endpoints
- POST /shorten?url=...&clickLimit=... - создание короткой ссылки
- GET /{shortCode} - редирект на оригинальный URL
- DELETE /{shortCode} - удаление ссылки
- PUT /{shortCode}/limit?newLimit=... - обновление лимита