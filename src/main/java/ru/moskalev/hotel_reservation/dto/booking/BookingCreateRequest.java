package ru.moskalev.hotel_reservation.dto.booking;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "Запрос на создание бронирования")
public record BookingCreateRequest(

        @NotNull(message = "ID комнаты обязателен")
        @Schema(description = "Идентификатор комнаты для бронирования",
                example = "5",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Long roomId,

        @NotNull(message = "Дата заезда обязательна")
        @Schema(description = "Дата заезда",
                example = "2026-07-10",
                requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate startDate,

        @NotNull(message = "Дата выезда обязательна")
        @Schema(description = "Дата выезда",
                example = "2026-07-15",
                requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate endDate,

        @Min(value = 1, message = "Количество гостей должно быть не менее 1")
        @Schema(description = "Количество гостей",
                example = "2",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minimum = "1")
        int guestCount

) {
    @AssertTrue(message = "Дата заезда должна быть раньше даты выезда")
    @Schema(hidden = true)
    public boolean isValidDates() {
        return startDate != null && endDate != null && startDate.isBefore(endDate);
    }
}