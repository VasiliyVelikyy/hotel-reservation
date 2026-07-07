package ru.moskalev.hotel_reservation.dto.booking;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Ответ с информацией о бронировании")
public record BookingResponse(
        @Schema(description = "Уникальный идентификатор бронирования",
                example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,

        @Schema(description = "Дата заезда",
                example = "2026-07-10",
               requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate startDate,

        @Schema(description = "Дата выезда",
                example = "2026-07-15",
               requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate endDate,

        @Schema(description = "Количество гостей",
                example = "2",
               requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "1")
        int guestCount,

        @Schema(description = "Идентификатор забронированной комнаты",
                example = "5",
               requiredMode = Schema.RequiredMode.REQUIRED)
        Long roomId,

        @Schema(description = "Идентификатор пользователя, осуществившего бронирование",
                example = "100",
               requiredMode = Schema.RequiredMode.REQUIRED)
        Long userId
) {
}