package ru.moskalev.hotel_reservation.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import ru.moskalev.hotel_reservation.enumeration.UserRole;

@Schema(description = "Входные данные для создания пользователя")
public record UserCreateInput(
        @Schema(description = "Уникальное имя пользователя", example = "testLogin")
        @NotBlank(message = "Логин не может быть пустым")
        @Size(min = 3, max = 50, message = "Логин должен быть от 3 до 50 символов")
        String login,

        @Schema(description = "Электронная почта", example = "test@mail.dev")
        @NotBlank(message = "Email не может быть пустым")
        @Size(max = 100, message = "Email не может превышать 100 символов") // ✅ Исправлено с @Max
        @Email(message = "Некорректный формат email")
        String email,

        @Schema(description = "Пароль", example = "qwertyui")
        @NotBlank(message = "Пароль не может быть пустым")
        @Size(min = 8, max = 128, message = "Пароль должен быть от 8 до 128 символов")
        String password,

        @Schema(description = "Роль пользователя", example = "CLIENT", allowableValues = {"ADMIN", "CLIENT"})
        @NotNull(message = "Роль обязательна")
        UserRole role
) {
}
