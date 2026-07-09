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
import ru.moskalev.hotel_reservation.dto.hotel.HotelCreateInput;
import ru.moskalev.hotel_reservation.dto.hotel.HotelFilter;
import ru.moskalev.hotel_reservation.dto.hotel.HotelResponse;
import ru.moskalev.hotel_reservation.dto.hotel.HotelUpdateInput;

@Tag(name = "Hotels", description = "Операции с отелями")
public interface HotelApi {

    @Operation(
            summary = "Создать отель",
            description = "Создаёт новый отель в системе и возвращает его данные"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Отель успешно создан",
                    content = @Content(schema = @Schema(implementation = HotelResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Невалидные входные данные",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    HotelResponse create(@RequestBody(description = "Данные нового отеля", required = true)
                         @Valid HotelCreateInput input
    );

    @Operation(
            summary = "Получить отель по ID",
            description = "Возвращает данные отеля по его идентификатору"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Отель найден",
                    content = @Content(schema = @Schema(implementation = HotelResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Отель не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    HotelResponse getById(@Parameter(
                                  description = "Идентификатор отеля",
                                  required = true,
                                  example = "1",
                                  in = ParameterIn.PATH
                          )
                          @PathVariable Long hotelId
    );

    @Operation(summary = "Получить список всех отелей с пагинацией")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Отели успешно найдены",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Отели не найдены",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping
    Page<HotelResponse> getAll(@Parameter(description = "Номер страницы (начинается с 0)", example = "0") int page,
                               @Parameter(description = "Размер страницы", example = "10") int size,
                               @Parameter(description = "Поле для сортировки", example = "id") String sortBy,
                               @Parameter(description = "Направление сортировки (asc/desc)", example = "asc") String direction);

    @Operation(
            summary = "Получить список отелей с фильтрацией",
            description = "Возвращает страницу отелей с возможностью фильтрации по городу, названию, расстоянию и рейтингу. " +
                    "Если фильтр не передан или все его поля пустые — возвращаются все отели."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Отели успешно найдены",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Невалидные параметры фильтрации или пагинации",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    Page<HotelResponse> getAllByFilter(@Parameter(description = "Номер страницы (начинается с 0)", example = "0") int page,
                                       @Parameter(description = "Размер страницы", example = "10") int size,
                                       @Parameter(description = "Поле для сортировки", example = "id") String sortBy,
                                       @Parameter(description = "Направление сортировки (asc/desc)", example = "asc") String direction,
                                       @RequestBody(description = "Фильтр для поиска отелей. Все поля опциональны.")
                                       @Valid HotelFilter filter);

    @Operation(
            summary = "Обновить отель",
            description = "Частично обновляет данные существующего отеля"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Отель успешно обновлён",
                    content = @Content(schema = @Schema(implementation = HotelResponse.class))
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
    HotelResponse update(@Parameter(description = "Идентификатор отеля", required = true, example = "1", in = ParameterIn.PATH)
                         @PathVariable Long hotelId,
                         @RequestBody(description = "Данные для обновления отеля", required = true)
                         @Valid HotelUpdateInput input
    );

    @Operation(
            summary = "Удалить отель",
            description = "Удаляет отель по идентификатору"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Отель успешно удалён"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Отель не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    void delete(@Parameter(description = "Идентификатор отеля", required = true, example = "1", in = ParameterIn.PATH)
                @PathVariable Long hotelId
    );
}