package ru.moskalev.hotel_reservation.dto.room;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Ответ с данными номера")
public record RoomResponse(

        @Schema(description = "Идентификатор номера", example = "1")
        Long id,

        @Schema(description = "Название номера", example = "Люкс")
        String name,

        @Schema(description = "Описание номера")
        String description,

        @Schema(description = "Номер комнаты", example = "101")
        Short number,

        @Schema(description = "Цена за ночь", example = "5000.00")
        BigDecimal price,

        @Schema(description = "Максимальное количество гостей", example = "2")
        Byte maxCount,

        @Schema(description = "Идентификатор отеля", example = "1")
        Long hotelId
) {
}
