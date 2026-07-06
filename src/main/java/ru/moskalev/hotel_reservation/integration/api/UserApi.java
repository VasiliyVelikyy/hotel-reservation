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
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.moskalev.hotel_reservation.dto.ErrorResponse;
import ru.moskalev.hotel_reservation.dto.user.UserCreateInput;
import ru.moskalev.hotel_reservation.dto.user.UserResponse;
import ru.moskalev.hotel_reservation.dto.user.UserUpdateInput;

@Tag(name = "Users", description = "Операции с пользователями")
public interface UserApi {

    @Operation(
            summary = "Получить пользователя по логину",
            description = "Возвращает данные пользователя по его уникальному логину"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь найден",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь с таким логином не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    UserResponse getUserByLogin(@Parameter(
                                        description = "Уникальный логин пользователя",
                                        required = true,
                                        example = "ivan.petrov",
                                        in = ParameterIn.PATH
                                )
                                String login
    );

    @Operation(
            summary = "Получить пользователя по ID",
            description = "Возвращает данные пользователя по его идентификатору"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь найден",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    UserResponse getUserById(@Parameter(
                                     description = "Идентификатор пользователя",
                                     required = true,
                                     example = "1",
                                     in = ParameterIn.PATH
                             )
                             @PathVariable Long userId
    );

    @Operation(
            summary = "Создать пользователя",
            description = "Создаёт нового пользователя в системе и возвращает его данные"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Пользователь успешно создан",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Невалидные входные данные",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Пользователь с таким логином уже существует",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    UserResponse create(@RequestBody(description = "Данные нового пользователя", required = true)
                        @Valid UserCreateInput input
    );

    @Operation(
            summary = "Обновить пользователя",
            description = "Обновляет данные существующего пользователя и возвращает его обновлённую версию"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно обновлён",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Невалидные входные данные",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Логин уже занят другим пользователем",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    UserResponse update(@Parameter(
                                description = "Идентификатор пользователя",
                                required = true,
                                example = "1",
                                in = ParameterIn.PATH
                        )
                        @PathVariable Long userId,
                        @RequestBody(description = "Данные для обновления пользователя", required = true)
                        @Valid UserUpdateInput input
    );

    @Operation(
            summary = "Удалить пользователя",
            description = "Полностью удаляет пользователя из системы по его идентификатору"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Пользователь успешно удалён",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@Parameter(
                        description = "Идентификатор пользователя",
                        required = true,
                        example = "1",
                        in = ParameterIn.PATH
                )
                @PathVariable Long userId
    );
}