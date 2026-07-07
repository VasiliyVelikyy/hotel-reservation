package ru.moskalev.hotel_reservation.integration.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import ru.moskalev.hotel_reservation.dto.ErrorResponse;
import ru.moskalev.hotel_reservation.dto.booking.BookingCreateRequest;
import ru.moskalev.hotel_reservation.dto.booking.BookingResponse;

import java.util.List;

@Tag(name = "Bookings", description = "Операции с бронированиями")
public interface BookingApi {
    @Operation(
            summary = "Забронировать комнату",
            description = "Создаёт новое бронирование комнаты на указанные даты от имени текущего пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Бронирование успешно создано",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Невалидные входные данные",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Комната не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Комната уже забронирована на указанные даты",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    BookingResponse create(
            @RequestBody(description = "Данные нового бронирования", required = true)
            @Valid BookingCreateRequest input
    );

    @Operation(
            summary = "Получить бронирование по ID",
            description = "Возвращает данные бронирования по его идентификатору"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Бронирование найдено",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Бронирование не найдено",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    BookingResponse getById(
            @Parameter(
                    description = "Идентификатор бронирования",
                    required = true,
                    example = "1",
                    in = ParameterIn.PATH
            )
            @PathVariable Long bookingId
    );

    @Operation(
            summary = "Получить мои бронирования",
            description = "Возвращает список всех бронирований текущего аутентифицированного пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список бронирований текущего пользователя",
                    content = @Content(schema = @Schema(implementation = BookingResponse.class))
            )
    })
    List<BookingResponse> getMyBookings();

    @Operation(
            summary = "Отменить бронирование",
            description = "Удаляет бронирование по его идентификатору. Доступно только владельцу бронирования"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Бронирование успешно отменено"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Нет прав на отмену чужого бронирования",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Бронирование не найдено",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    void cancel(@Parameter(
                    description = "Идентификатор бронирования",
                    required = true,
                    example = "1",
                    in = ParameterIn.PATH
            )
            @PathVariable Long bookingId
    );


}
