# Hotel Reservation Service

[![Java](https://img.shields.io/badge/Java-21+-orange?logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.x-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)](https://www.postgresql.org/)
[![MongoDB](https://img.shields.io/badge/MongoDB-7.0-green?logo=mongodb)](https://www.mongodb.com/)
[![Kafka](https://img.shields.io/badge/Apache_Kafka-3.8-black?logo=apache-kafka)](https://kafka.apache.org/)
[![License](https://img.shields.io/badge/License-All%20Rights%20Reserved-red)](LICENSE)

Сервис бронирования номеров в отелях с событийной архитектурой, аутентификацией через Base auth и аналитикой на основе
Apache Kafka.

---

## PROJECT

Система позволяет пользователям регистрироваться, искать доступные номера, бронировать их на выбранные даты и получать
статистику по своим действиям. Проект построен по принципам **Event-Driven Architecture**: все ключевые бизнес-события (
регистрация пользователя, бронирование номера) асинхронно публикуются в Kafka и агрегируются в аналитическом хранилище
MongoDB.

### Ключевые возможности

- **Управление пользователями** — регистрация, аутентификация, роли (`ADMIN`, `CLIENT`)
- **Управление номерами** — CRUD операций с номерами отелей
- **Бронирование** — проверка доступности дат, защита от овербукинга (pessimistic lock)
- **Безопасность** — Spring Security , разграничение доступа по ролям
- **Аналитика** — сбор событий в MongoDB с возможностью экспорта в CSV
- **Асинхронность** — отправка событий в Kafka строго после коммита транзакции (
  `TransactionSynchronization.afterCommit`)
- **API документация** — автоматическая генерация Swagger/OpenAPI

## ARCHITECTURE

```mermaid
graph TB
    subgraph Client[" Клиент"]
        POSTMAN[Postman / Frontend]
    end

    subgraph SpringBoot[" Spring Boot Application"]
        direction TB
        CONTROLLER["REST Controllers<br/>(UserController, BookingController,<br/>RoomController, StatsController,<br/> HotelController)"]
        SERVICE["Service Layer<br/>(UserService, BookingService,<br/>RoomService, StatsService<br/> CsvExportService, HotelService)"]
        PRODUCER["KafkaStatsPublisher<br/>(@Component)"]
        CONSUMER["KafkaStatsConsumer<br/>(@KafkaListener)"]
        SECURITY["Spring Security<br/>(Base auth)"]
        CONTROLLER --> SERVICE
        SERVICE --> PRODUCER
        CONSUMER --> SERVICE
        CONTROLLER --> SECURITY
    end

    subgraph DataStores[" Хранилища данных"]
        direction LR
        PG[("PostgreSQL<br/>Users, Rooms, Bookings,<br/> Hotel, Grade ")]
        MONGO[("MongoDB<br/>StatEventDocument")]
    end

    subgraph Broker["Брокер сообщений"]
        KAFKA{{"Apache Kafka<br/>Topics: user-registration,<br/>room-booking"}}
    end

%% Потоки данных
    POSTMAN -->|" HTTP/JSON "| CONTROLLER
    CONTROLLER -->|" JPA/SQL "| PG
    SERVICE -->|" MongoRepository "| MONGO
    PRODUCER -->|" KafkaTemplate.send() "| KAFKA
    KAFKA -->|" @KafkaListener "| CONSUMER
%% Стили
    classDef client fill: #e1f5fe, stroke: #01579b, stroke-width: 2px
    classDef app fill: #fff3e0, stroke: #e65100, stroke-width: 2px
    classDef db fill: #e8f5e9, stroke: #1b5e20, stroke-width: 2px
    classDef broker fill: #fce4ec, stroke: #880e4f, stroke-width: 2px
    class POSTMAN client
class CONTROLLER, SERVICE, PRODUCER, CONSUMER, SECURITY app
class PG,MONGO db
class KAFKA broker
```

## TECHNOLOGY

| Категория            | Технология                                   |
|----------------------|----------------------------------------------|
| **Язык**             | Java 21                                      |
| **Фреймворк**        | Spring Boot 4.0.x (Web MVC)                  |
| **ORM / БД**         | Spring Data JPA + PostgreSQL                 |
| **Документная БД**   | Spring Data MongoDB + MongoDB 7.0            |
| **Брокер сообщений** | Spring Kafka + Apache Kafka 3.8              |
| **Безопасность**     | Spring Security (Base auth)                  |
| **Миграции БД**      | Liquibase                                    |
| **Маппинг**          | MapStruct                                    |
| **Валидация**        | Jakarta Validation (Hibernate Validator)     |
| **Генерация CSV**    | OpenCSV                                      |
| **Утилиты**          | Lombok                                       |
| **API-документация** | SpringDoc OpenAPI (Swagger UI)               |
| **Тестирование**     | JUnit 5, Mockito, Testcontainers, Awaitility |
| **Сборка**           | Gradle (Kotlin DSL)                          |
| **Контейнеризация**  | Docker, Docker Compose                       |

## FAST START

### Требования

- Java 21+
- Docker & Docker Compose
- Gradle 8+

### 1. Cloning repository

```bash
git clone https://github.com/VasiliyVelikyy/hotel-reservation.git
```

### 2. Запуск инфраструктуры через Docker Compose

```bash
  cd D:\IdeaProjects\hotel-reservation
  docker compose up -d
```

Это поднимет:

- Приложение на порту `8090`
- PostgreSQL на порту `5432`
- MongoDB на порту `27017`

### 3. Start app

* Перед стартом приложения, необходимо выполнить создание схемы

```sql
CREATE SCHEMA IF NOT EXISTS hotel_reservation
```

Запуск приложения

```bash
./gradlew bootRun
```

Приложение будет доступно по адресу: **http://localhost:8090**

## API Documentation

После запуска приложения Swagger UI доступен по адресу:

**http://localhost:8090/swagger-ui**

### HTTP-FILES

В директории `/http` находятся готовые `.http` файлы для тестирования API через IntelliJ IDEA:

```
http/
├── user.http         # Регистрация, логин, обновление и управление пользователями
├── room.http         # CRUD номеров
├── hotel.http         # CRUD отелей
├── grade.http         # Выставление рейтинга отелю
├── booking.http      # Бронирование (сценарии успеха и ошибок)
└── stats.http         # Получение статистики и экспорт в CSV
```

> Для работы с защищёнными эндпоинтами сначала выполните запрос `POST /v1/user/login``.

---

## Testing

Проект покрыт интеграционными тестами с использованием **Testcontainers** — для каждого прогона тестов поднимаются
реальные контейнеры PostgreSQL, MongoDB и Kafka.

### Running tests

```bash
./gradlew test
```

### Типы тестов

| Тип                      | Описание                                               |
|--------------------------|--------------------------------------------------------|
| **Unit-тесты**           | Покрытие сервисов и мапперов (Mockito)                 |
| **Интеграционные тесты** | Проверка связки Service → DB / Service → Kafka → Mongo |
| **API-тесты**            | End-to-end через `MockMvc`                             |

## ️ Configuration

Основные переменные окружения (`application.yml`):

## Endpoints API

> Базовый путь: `/v1`

### Auth and users (`/user`)

| Метод    | Путь                  | Описание                        | Доступ    |
|----------|-----------------------|---------------------------------|-----------|
| `POST`   | `/user`               | Регистрация нового пользователя | Публичный |
| `GET`    | `/user/login/{login}` | Получить пользователя по логину | Публичный |
| `GET`    | `/user/{userId}`      | Получить пользователя по ID     | Публичный |
| `PUT`    | `/user/{userId}`      | Обновить данные пользователя    | Публичный |
| `DELETE` | `/user/{userId}`      | Удалить пользователя            | Публичный |

### Отели (`/hotel`)

| Метод    | Путь                     | Описание                       | Доступ |
|----------|--------------------------|--------------------------------|--------|
| `POST`   | `/hotel`                 | Создать новый отель            | ADMIN  |
| `GET`    | `/hotel/{hotelId}`       | Получить отель по ID           | Auth   |
| `GET`    | `/hotel`                 | Список всех отелей (пагинация) | Auth   |
| `POST`   | `/hotel/filter`          | Поиск отелей по фильтру        | Auth   |
| `PUT`    | `/hotel/{hotelId}`       | Обновить информацию об отеле   | ADMIN  |
| `DELETE` | `/hotel/{hotelId}`       | Удалить отель                  | ADMIN  |
| `POST`   | `/hotel/{hotelId}/grade` | Оценить отель (1–5)            | Auth   |

### Номера (`/room`)

| Метод    | Путь                           | Описание                     | Доступ |
|----------|--------------------------------|------------------------------|--------|
| `POST`   | `/room/{hotelId}`              | Создать номер в отеле        | ADMIN  |
| `GET`    | `/room/{roomId}`               | Получить номер по ID         | Auth   |
| `GET`    | `/room/hotel/{hotelId}`        | Все номера отеля (пагинация) | Auth   |
| `POST`   | `/room/hotel/{hotelId}/filter` | Фильтр номеров отеля         | Auth   |
| `PUT`    | `/room/{roomId}`               | Обновить номер               | ADMIN  |
| `DELETE` | `/room/{roomId}`               | Удалить номер                | ADMIN  |

### Бронирования (`/booking`)

| Метод    | Путь                   | Описание                     | Доступ |
|----------|------------------------|------------------------------|--------|
| `POST`   | `/booking`             | Создать бронирование         | Auth   |
| `GET`    | `/booking/{bookingId}` | Получить бронирование по ID  | Auth   |
| `GET`    | `/booking/my`          | Мои бронирования             | Auth   |
| `GET`    | `/booking`             | Все бронирования (пагинация) | ADMIN  |
| `DELETE` | `/booking/{bookingId}` | Отменить бронирование        | Auth   |

### Статистика (`/stats`)

| Метод | Путь                | Описание                      | Доступ |
|-------|---------------------|-------------------------------|--------|
| `GET` | `/stats/export/csv` | Экспорт статистики в CSV-файл | ADMIN  |

### Параметры пагинации (для `GET` со списком)

Все эндпоинты с пагинацией поддерживают следующие query-параметры:

| Параметр    | По умолчанию | Описание                     |
|-------------|--------------|------------------------------|
| `page`      | `0`          | Номер страницы (с нуля)      |
| `size`      | `20`         | Размер страницы              |
| `sortBy`    | `id`         | Поле для сортировки          |
| `direction` | `ASC`        | Направление (`ASC` / `DESC`) |

## CI

[![CI Pipeline](https://github.com/VasiliyVelikyy/hotel-reservation/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/VasiliyVelikyy/hotel-reservation/actions/workflows/ci.yml)

## TEST COVERAGE

[![Coverage](https://codecov.io/gh/VasiliyVelikyy/hotel-reservation/branch/master/graph/badge.svg?token=c0d69fc9-158d-479d-87fd-6e41da956019)](https://codecov.io/gh/VasiliyVelikyy/hotel-reservation)

### Запуск тестов с покрытием

```bash
./gradlew test jacocoTestReport
````

Этот проект использует **JaCoCo** для измерения покрытия тестами.
Подробный отчет доступен на [Codecov Dashboard](https://app.codecov.io/github/VasiliyVelikyy/hotel-reservation).

## Testing

Мы используем Codecov для отслеживания покрытия кода тестами.
При создании Pull Request автоматически проверяется покрытие и оставляется комментарий с деталями.

**Требования к покрытию:**

- Минимальное покрытие: 80%
- Покрытие не должно уменьшаться более чем на 5%

## LICENSE

This project is distributed under a proprietary license. All rights reserved.
For more details, see the [LICENSE](LICENSE) file.

## AUTHOR

**MOSKALEV VASILIY**

## CONTACTS

[![Email](https://img.shields.io/badge/Email-vasian_vrn%40mail.ru-EA4335?logo=gmail)](mailto:vasian_vrn@mail.ru)
[![Telegram](https://img.shields.io/badge/Telegram-%40vasiliyVelikiyy-blue?logo=telegram)](https://t.me/vasiliyVelikiyy)
[![HeadHunter](https://img.shields.io/badge/HeadHunter-Resume-D6001C?logo=headhunter)](https://voronezh.hh.ru/resume/dab91c46ff092418870039ed1f5769426f654a)
[![Habr Career](https://img.shields.io/badge/Habr%20Career-Profile-65A3BE?logo=habr)](https://career.habr.com/alg0_rith_mock)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Vasily%20Moskalev-0A66C2?logo=linkedin)](https://www.linkedin.com/in/vasiliy-moskalev-ba8b84421/)
[![GitHub](https://img.shields.io/badge/GitHub-VasiliyVelikyy-black?logo=github)](https://github.com/VasiliyVelikyy)




<div style="text-align: center;">
  <sub>Made with ❤️ and ☕</sub>
</div>
