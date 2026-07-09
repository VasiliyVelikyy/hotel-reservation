package ru.moskalev.hotel_reservation.dto.grade;

import io.swagger.v3.oas.annotations.media.Schema;

public record GradeResponse(
        @Schema(description = "Идентификатор отеля",
                example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Long hotelId,

        @Schema(description = "Текущий рейтинг",
                example = "5.00",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Double rating,

        @Schema(description = "Сумма всех оценок",
                example = "50",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Double totalRating,

        @Schema(description = "Общее количество оценок",
                example = "10",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer numberOfRating) {
}
