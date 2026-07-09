package ru.moskalev.hotel_reservation.dto.room;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

@Schema(description = "Фильтр для поиска комнат")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RoomFilter(
        @Schema(description = "ID комнаты", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Long roomId,

        @Schema(description = "Часть названия комнаты (поиск)", example = "Luxury", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String nameContains,

        @Schema(description = "Минимальная цена", example = "1000.00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @DecimalMin(value = "0.0", message = "Минимальная цена не может быть отрицательной")
        BigDecimal minPrice,

        @Schema(description = "Максимальная цена", example = "10000.00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @DecimalMin(value = "0.0", message = "Максимальная цена не может быть отрицательной")
        BigDecimal maxPrice,

        @Schema(description = "Минимальная вместимость гостей", example = "2", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Min(value = 1, message = "Минимальная вместимость должна быть не менее 1")
        Integer minGuestCount,

        @Schema(description = "Дата заезда (в миллисекундах с эпохи)", example = "1751932800000", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Long startDate,

        @Schema(description = "Дата выезда (в миллисекундах с эпохи)", example = "1752537600000", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Long endDate
) {
    @AssertTrue(message = "minPrice должно быть меньше или равно maxPrice")
    @Schema(hidden = true)
    public boolean isValidPriceRange() {
        if (minPrice == null || maxPrice == null) {
            return true;
        }
        return minPrice.compareTo(maxPrice) <= 0;
    }

    @AssertTrue(message = "Дата заезда должна быть раньше даты выезда")
    @Schema(hidden = true)
    public boolean isValidDates() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return startDate < endDate;
    }
}