package ru.moskalev.hotel_reservation.dto.room;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Данные для частичного обновления номера")
public record RoomUpdateInput(

        @Schema(description = "Название номера")
        @Size(max = 100)
        String name,

        @Schema(description = "Описание номера")
        @Size(max = 255)
        String description,

        @Schema(description = "Номер комнаты")
        Short number,

        @Schema(description = "Цена за ночь")
        @Positive
        BigDecimal price,

        @Schema(description = "Максимальное количество гостей")
        @Positive
        Byte maxCount,

        @Schema(description = "Дата начала свободного периода (в миллисекундах)")
        @Positive
        Long freeStartDate,

        @Schema(description = "Дата окончания свободного периода (в миллисекундах)")
        @Positive
        Long freeEndDate
) {
}