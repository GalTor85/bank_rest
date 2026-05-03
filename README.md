## Описание проекта
**Bank REST API** – система управления банковскими картами.

Учебный проект на Spring Boot, реализующий REST API.  
Поддерживает аутентификацию и авторизацию (Spring Security + JWT, роли ADMIN и USER),  
создание, блокировку, активацию, удаление карт, переводы между своими картами,  
управление пользователями (ADMIN), шифрование номеров карт, пагинацию, Swagger-документацию.

## Технологии
Java 17, Spring Boot 4.0.6, Spring Security, JWT, Spring Data JPA, PostgreSQL, Liquibase, Docker, Swagger.

## Быстрый старт
1. **Запустите PostgreSQL через Docker**
   ```bash
   docker-compose up -d
   ```
2. **Запустите приложение**
   ```bash
   mvn spring-boot:run
   ```
3. Откройте Swagger UI 
http://localhost:8081/swagger-ui.html

### Первый администратор

Регистрация доступна только администратору, поэтому первого ADMIN нужно создать вручную.
Выполните SQL-запрос к базе данных (PostgreSQL):
```sql
INSERT INTO users (username, password, role) VALUES ('admin', '$2a$10$...', 'ADMIN');
```
Хеш пароля сгенерируйте заранее (например, через BCryptPasswordEncoder).

## Документация

Swagger UI генерируется автоматически на основе аннотаций в коде.
Статическая спецификация также хранится в файле docs/openapi.yaml.

Генерация документации
Статический файл можно обновить из запущенного приложения:

Windows PowerShell: 
```bash
Invoke-WebRequest -Uri http://localhost:8081/v3/api-docs.yaml -OutFile docs/openapi.yaml
```

Linux/macOS: 

```bash
curl -s http://localhost:8081/v3/api-docs.yaml -o docs/openapi.yaml
```
  
## Тестирование

Запустите unit-тесты командой:
```bash
mvn test
```