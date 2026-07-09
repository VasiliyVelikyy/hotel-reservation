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
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.moskalev.hotel_reservation.dto.ErrorResponse;
import ru.moskalev.hotel_reservation.dto.room.RoomCreateInput;
import ru.moskalev.hotel_reservation.dto.room.RoomFilter;
import ru.moskalev.hotel_reservation.dto.room.RoomResponse;
import ru.moskalev.hotel_reservation.dto.room.RoomUpdateInput;

@Tag(name = "Rooms", description = "Операции с номерами отелей")
public interface RoomApi {

    @Operation(
            summary = "Создать номер",
            description = "Создаёт новый номер в отеле и возвращает его данные"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Номер успешно создан",
                    content = @Content(schema = @Schema(implementation = RoomResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Невалидные входные данные",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Отель не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    RoomResponse create(@Parameter(
                                description = "Идентификатор отеля",
                                required = true,
                                example = "1",
                                in = ParameterIn.PATH
                        )
                        Long hotelId,
                        @RequestBody(description = "Данные нового номера", required = true)
                        @Valid RoomCreateInput input
    );

    @Operation(
            summary = "Получить номер по ID",
            description = "Возвращает данные номера по его идентификатору"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Номер найден",
                    content = @Content(schema = @Schema(implementation = RoomResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Номер не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    RoomResponse getById(@Parameter(
                                 description = "Идентификатор номера",
                                 required = true,
                                 example = "1",
                                 in = ParameterIn.PATH
                         )
                         @PathVariable Long roomId
    );

    @Operation(summary = "Получить список всех номеров отеля с пагинацией")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Номера успешно найдены",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Номера не найдены",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping
    Page<RoomResponse> getAllByHotelId(@Parameter(
                                               description = "Идентификатор отеля",
                                               required = true,
                                               example = "1",
                                               in = ParameterIn.PATH
                                       )
                                       @PathVariable Long hotelId,
                                       @Parameter(description = "Номер страницы", example = "0") int page,
                                       @Parameter(description = "Размер страницы", example = "10") int size,
                                       @Parameter(description = "Поле сортировки", example = "id") String sortBy,
                                       @Parameter(description = "Направление сортировки", example = "asc") String direction);


    @Operation(
            summary = "Получить комнаты отеля с фильтрацией",
            description = "Возвращает страницу комнат указанного отеля с возможностью фильтрации. " +
                    "Если указаны даты заезда и выезда — возвращаются только свободные комнаты."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Комнаты успешно найдены"),
            @ApiResponse(responseCode = "400", description = "Невалидные параметры"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "404", description = "Отель не найден")
    })
    Page<RoomResponse> getAllByHotelIdAndFilter(
            @Parameter(description = "ID отеля", example = "1")
            @PathVariable Long hotelId,
            @Parameter(description = "Номер страницы", example = "0") int page,
            @Parameter(description = "Размер страницы", example = "10") int size,
            @Parameter(description = "Поле сортировки", example = "id") String sortBy,
            @Parameter(description = "Направление сортировки", example = "asc") String direction,
            @RequestBody(description = "Фильтр комнат. Все поля опциональны. " +
                    "Если указаны startDate и endDate — возвращаются только свободные комнаты.")
            @Valid RoomFilter filter
    );

    @Operation(
            summary = "Обновить номер",
            description = "Частично обновляет данные существующего номера"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Номер успешно обновлён",
                    content = @Content(schema = @Schema(implementation = RoomResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Невалидные входные данные",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Номер не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    RoomResponse update(@Parameter(description = "Идентификатор номера", required = true, example = "1", in = ParameterIn.PATH)
                        Long roomId,
                        @RequestBody(description = "Данные для обновления номера", required = true)
                        @Valid RoomUpdateInput input
    );

    @Operation(
            summary = "Удалить номер",
            description = "Удаляет номер по идентификатору"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Номер успешно удалён"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Номер не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    void delete(@Parameter(description = "Идентификатор номера", required = true, example = "1", in = ParameterIn.PATH)
                @PathVariable Long roomId
    );
}