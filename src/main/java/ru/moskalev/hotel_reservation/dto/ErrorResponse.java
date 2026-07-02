package ru.moskalev.hotel_reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Стандартный ответ с информацией об ошибке")
public record ErrorResponse(

        @Schema(description = "HTTP-статус код", example = "404")
        int statusCode,

        @Schema(description = "Краткое описание типа ошибки", example = "Not Found")
        String message,

        @Schema(description = "Детальное описание ошибки", example = "Hotel with id 5 not found")
        String description,

        @Schema(description = "Время возникновения ошибки", example = "2026-07-02T12:34:56")
        LocalDateTime timestamp
) {
    public ErrorResponse(int statusCode, String message, String description) {
        this(statusCode, message, description, LocalDateTime.now());
    }
}