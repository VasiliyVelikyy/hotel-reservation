package ru.moskalev.hotel_reservation.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import ru.moskalev.hotel_reservation.annotation.Masked;
import ru.moskalev.hotel_reservation.enumeration.MaskType;
import ru.moskalev.hotel_reservation.enumeration.UserRole;

@Schema(description = "Ответ с данными пользователя (без пароля)")
public record UserResponse(

        @Schema(description = "Уникальный идентификатор пользователя", example = "1")
        @NotNull
        Long id,

        @Schema(description = "Имя пользователя", example = "testLogin")
        @NotNull
        @Masked(MaskType.LOGIN)
        String login,

        @Schema(description = "Электронная почта пользователя", example = "test@mail.dev")
        @NotNull
        @Masked(MaskType.EMAIL)
        String email,

        @Schema(description = "Роль пользователя", example = "CLIENT", allowableValues = {"ADMIN", "CLIENT"})
        @NotNull
        UserRole role
) {
}