# Поднять контейнеры докера

```shell
cd D:\IdeaProjects\hotel-reservation
docker compose up -d
```

* Перед стартом приложения, необходимо выполнить

```sql
CREATE SCHEMA IF NOT EXISTS hotel_reservation
```

# Сваггер
> <http://localhost:8090/swagger-ui>


# Последователность действий
## 1. Создание юзера с ролью ADMIN
Смотреть файл rest/user.http 
Либо через свагер, ендпоинт регистрации доступен всем
```text
@host = http://localhost:8090/v1/user
### 1. Создание пользователя (POST)

```

## 2. Создание отеля (может выполнять только ADMIN)

Смотреть файл rest/hotel.http 
```text
@host = http://localhost:8090/v1/hotels

### 1. Создание отеля (POST)
```


```shell
docker exec -it kafka rpk group seek hotel-reservation-group --to start --topics user-registration-topic --topics room-booking-topic --allow-new-topics

```