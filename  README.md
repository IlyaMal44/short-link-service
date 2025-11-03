# ShortLink Service
Сервис сокращения ссылок с ограничением переходов и временем жизни.

## Возможности
- Создание коротких ссылок
- Ограничение количества переходов
- Автоматическое удаление просроченных ссылок
- Уникальные ссылки для каждого пользователя
- REST API + CLI интерфейс
----
##  Технологии
- Java 17
- Spring Boot 3.x
- H2 Database
- JPA/Hibernate
- Gradle
- JUnit 5 + Mockito
----
## Запуск приложения и тестов:
#### Запуск приложения:
1) Через IDE (IntelliJ): Откройте ShortLinkApplication.java и далее нажмите Run 
2) Через коносоль: Сначала соберите JAR  ./gradlew build, а затем запустите напрямую  java -jar build/libs/shortLink-0.0.1-SNAPSHOT.jar 
#### Запуск тестов:
1) Через IDE (IntelliJ): Нажмите Run на пакете test → Произойдет запуск всех тестов
2) Через коносоль: (`./gradlew test`)
----
### Требования
- Java 17+
- Gradle
----
##  Архитектура
1. config/ - AppConfig (настройки), ConsoleClient (CLI)
2. controller/ - LinkController (REST API)
3. domain/entity/ - LinkEntity, UserEntity (модели данных)
4. domain/repository/ - LinkRepository, UserRepository (доступ к БД)
5. service/ - LinkService, UserService, NotificationService (бизнес-логика)
6. utils/StringUtils/ - утилиты для работы со строками
7. test/ - тесты (JUnit 5 + Mockito)
----

## CI/CD (GitHub Actions)

Проект настроен с автоматической системой непрерывной интеграции:

### Автоматические проверки:
- **При каждом push** и **pull request**
- **Сборка и тестирование** на Ubuntu + Java 17
- **Проверка качества кода**

### Workflow этапы:
1. **Checkout** - получение кода из репозитория
2. **JDK 17** - установка Java окружения
3. **Permissions** - настройка прав для Gradle Wrapper
4. **Tests** - запуск всех unit-тестов (`./gradlew test`)
5. **Build** - сборка приложения (`./gradlew build`)

### Файл конфигурации:
`.github/workflows/build.yml`

### Мониторинг:
- Детальные логи доступны в Actions tab
- Уведомления о failed builds
----

## API Endpoints

1. POST /shorten?url={URL}&clickLimit={LIMIT} - Создание короткой ссылки

**Параметры:**
- url (обязательный) - оригинальный URL для сокращения
- clickLimit (опциональный) - максимальное количество переходов

**Заголовки:**
- X-User-ID (опциональный) - идентификатор пользователя
--------
2. GET /{shortCode} - редирект на оригинальный URL

**Параметры:**
- shortCode (обязательный) - код короткой ссылки
--------
3. DELETE /{shortCode} - удаление ссылки

**Параметры:**
- shortCode (обязательный) - код короткой ссылки

**Заголовки:**
- X-User-ID (обязательный) - идентификатор пользователя
--------
4) PUT /{shortCode}/limit?newLimit={NEW_LIMIT} - обновление лимита 

**Параметры:**
- newLimit (обязательный) - новое значение лимита

**Заголовки:**
- X-User-ID (обязательный) - идентификатор пользователя
--------
5) GET /user/links  -  Получение ссылок пользователя

**Заголовки:**
- X-User-ID (обязательный) - идентификатор пользователя
--------