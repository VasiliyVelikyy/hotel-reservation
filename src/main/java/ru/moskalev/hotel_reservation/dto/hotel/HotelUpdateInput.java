package ru.moskalev.hotel_reservation.dto.hotel;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Данные для частичного обновления отеля")
public record HotelUpdateInput(

        @Schema(description = "Название отеля")
        @Size(max = 50)
        String name,

        @Schema(description = "Описание отеля")
        @Size(max = 255)
        String description,

        @Schema(description = "Заголовок")
        @Size(max = 50)
        String title,

        @Schema(description = "Город")
        @Size(max = 50)
        String city,

        @Schema(description = "Город")
        @Size(max = 100)
        String address,

        @Schema(description = "Расстояние в метрах")
        Integer distance
) {
}