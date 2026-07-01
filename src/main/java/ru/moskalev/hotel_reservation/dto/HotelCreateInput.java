package ru.moskalev.hotel_reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Входные данные для создания отеля")
public record HotelCreateInput(

        @Schema(description = "Название отеля", example = "Grand Hotel")
        @NotBlank
        @Size(max = 50)
        String name,

        @Schema(description = "Описание отеля", nullable = true)
        @Size(max = 255)
        String description,

        @Schema(description = "Заголовок", nullable = true)
        @Size(max = 50)
        String title,

        @Schema(description = "Город")
        @Size(max = 50)
        @NotBlank
        String city,

        @Schema(description = "Город")
        @Size(max = 100)
        @NotBlank
        String address,

        @Schema(description = "Расстояние в метрах", example = "1500")
        @NotBlank
        Integer distance
) {
}
