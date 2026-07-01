package ru.moskalev.hotel_reservation.integration.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import ru.moskalev.hotel_reservation.dto.HotelCreateInput;
import ru.moskalev.hotel_reservation.dto.HotelResponse;
import ru.moskalev.hotel_reservation.dto.HotelUpdateInput;

@Tag(name = "Hotels", description = "Операции с отелями")
public interface HotelApi {

    @Operation(
            summary = "Создать отель",
            description = "Создаёт новый отель в системе и возвращает его данные"
    )
    @ApiResponse(responseCode = "201", description = "Отель успешно создан")
    @ApiResponse(responseCode = "400", description = "Невалидные входные данные")
    HotelResponse create(@RequestBody(description = "Данные нового отеля", required = true)
                         @Valid HotelCreateInput input
    );

    @Operation(
            summary = "Получить отель по ID",
            description = "Возвращает данные отеля по его идентификатору"
    )
    @ApiResponse(responseCode = "200", description = "Отель найден")
    @ApiResponse(responseCode = "404", description = "Отель не найден")
    HotelResponse getById(@Parameter(
                                  description = "Идентификатор отеля",
                                  required = true,
                                  example = "1",
                                  in = ParameterIn.PATH
                          )
                          @PathVariable Long hotelId
    );

    @Operation(
            summary = "Обновить отель",
            description = "Частично обновляет данные существующего отеля"
    )
    @ApiResponse(responseCode = "200", description = "Отель успешно обновлён")
    @ApiResponse(responseCode = "400", description = "Невалидные входные данные")
    @ApiResponse(responseCode = "404", description = "Отель не найден")
    HotelResponse update(@Parameter(description = "Идентификатор отеля", required = true, example = "1", in = ParameterIn.PATH)
                         @PathVariable Long hotelId,
                         @RequestBody(description = "Данные для обновления отеля", required = true)
                         @Valid HotelUpdateInput input
    );

    @Operation(
            summary = "Удалить отель",
            description = "Удаляет отель по идентификатору"
    )
    @ApiResponse(responseCode = "204", description = "Отель успешно удалён")
    @ApiResponse(responseCode = "404", description = "Отель не найден")
    void delete(@Parameter(description = "Идентификатор отеля", required = true, example = "1", in = ParameterIn.PATH)
                @PathVariable Long hotelId
    );
}