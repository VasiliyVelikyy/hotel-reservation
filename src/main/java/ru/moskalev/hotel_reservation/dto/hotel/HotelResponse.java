package ru.moskalev.hotel_reservation.dto.hotel;

import io.swagger.v3.oas.annotations.media.Schema;

public record HotelResponse(
        @Schema(description = "Идентификатор отеля", example = "1")
        Long id,
        @Schema(description = "Название отеля", example = "Grand Hotel")
        String name,

        @Schema(description = "Описание отеля", nullable = true)

        String description,

        @Schema(description = "Заголовок", nullable = true)
        String title,

        @Schema(description = "Город")
        String city,

        @Schema(description = "Город")
        String address,

        @Schema(description = "Расстояние в метрах", example = "1500")
        Integer distance
) {
}
