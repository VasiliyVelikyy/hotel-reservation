package ru.moskalev.hotel_reservation.dto.hotel;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

@Schema(description = "Фильтр для поиска отелей")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record HotelFilter(
        @Schema(description = "Город", example = "Moscow", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String city,

        @Schema(description = "Часть названия отеля (поиск)", example = "Grand", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String nameContains,

        @Schema(description = "Минимальное расстояние (в метрах)", example = "500", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Min(value = 0, message = "Минимальное расстояние не может быть отрицательным")
        Integer minDistance,

        @Schema(description = "Максимальное расстояние (в метрах)", example = "5000", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Min(value = 0, message = "Максимальное расстояние не может быть отрицательным")
        Integer maxDistance,

        @Schema(description = "Минимальный рейтинг", example = "4.0", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @DecimalMin(value = "0.0", message = "Рейтинг не может быть отрицательным")
        @DecimalMax(value = "5.0", message = "Рейтинг не может превышать 5.0")
        Double minRating
) {
    @AssertTrue(message = "minDistance должно быть меньше или равно maxDistance")
    @Schema(hidden = true)
    public boolean isValidDistanceRange() {
        if (minDistance == null || maxDistance == null) {
            return true;
        }
        return minDistance <= maxDistance;
    }
}