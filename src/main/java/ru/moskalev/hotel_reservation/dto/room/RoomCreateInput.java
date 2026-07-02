package ru.moskalev.hotel_reservation.dto.room;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Входные данные для создания номера")
public record RoomCreateInput(

        @Schema(description = "Название номера", example = "Люкс")
        @NotBlank
        @Size(max = 100)
        String name,

        @Schema(description = "Описание номера", nullable = true)
        @Size(max = 255)
        String description,

        @Schema(description = "Номер комнаты", example = "101")
        @NotNull
        Short number,

        @Schema(description = "Цена за ночь", example = "5000.00")
        @NotNull
        @Positive
        BigDecimal price,

        @Schema(description = "Максимальное количество гостей", example = "2")
        @NotNull
        @Positive
        Byte maxCount,

        @Schema(description = "Дата начала свободного периода (в миллисекундах)", example = "1700000000000")
        @NotNull
        @Positive
        Long freeStartDate,

        @Schema(description = "Дата окончания свободного периода (в миллисекундах)", example = "1700100000000")
        @NotNull
        @Positive
        Long freeEndDate
) {
}