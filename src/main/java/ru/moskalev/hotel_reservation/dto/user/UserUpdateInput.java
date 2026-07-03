package ru.moskalev.hotel_reservation.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import ru.moskalev.hotel_reservation.enumeration.UserRole;

@Schema(description = "Входные данные для обновления пользователя")
public record UserUpdateInput(

        @Schema(description = "Имя пользователя", example = "newLogin")
        @Size(min = 3, max = 50, message = "Логин должен быть от 3 до 50 символов")
        String login,

        @Schema(description = "Электронная почта", example = "new@mail.dev")
        @Size(max = 100, message = "Email не может превышать 100 символов")
        @Email(message = "Некорректный формат email")
        String email,

        @Schema(description = "Пароль (если нужно изменить)")
        @Size(min = 8, max = 128, message = "Пароль должен быть от 8 до 128 символов")
        String password,

        @Schema(description = "Роль пользователя", example = "ADMIN", allowableValues = {"ADMIN", "CLIENT"})
        UserRole role
) {
}