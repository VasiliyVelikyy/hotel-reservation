package ru.moskalev.hotel_reservation.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.moskalev.hotel_reservation.domain.User;
import ru.moskalev.hotel_reservation.dto.user.UserCreateInput;
import ru.moskalev.hotel_reservation.dto.user.UserResponse;
import ru.moskalev.hotel_reservation.dto.user.UserUpdateInput;
import ru.moskalev.hotel_reservation.enumeration.UserRole;
import ru.moskalev.hotel_reservation.exception.EntityAlreadyExistsException;
import ru.moskalev.hotel_reservation.exception.EntityNotFoundException;
import ru.moskalev.hotel_reservation.repo.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("UserService")
class UserServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @SneakyThrows
    @Test
    @DisplayName("create: должен создать пользователя и вернуть корректный DTO")
    void create_shouldSaveAndReturnDto() {
        // given
        UserCreateInput input = new UserCreateInput(
                "testLogin",
                "test@mail.dev",
                "password123",
                UserRole.CLIENT
        );

        // when
        UserResponse response = userService.create(input);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertEquals(input.role(), response.role());
        assertEquals(input.login(), response.login());
        assertEquals(input.email(), response.email());
    }

    @Test
    @DisplayName("create: должен выбросить исключение при дублировании email и login")
    void create_shouldThrowExceptionWhenEmailAndLoginExist() {
        // given
        UserCreateInput input1 = new UserCreateInput(
                "testLogin",
                "test@mail.dev",
                "password123",
                UserRole.CLIENT
        );
        userService.create(input1);

        UserCreateInput input2 = new UserCreateInput(
                "testLogin",
                "test@mail.dev",
                "password456",
                UserRole.ADMIN
        );

        // when & then
        assertThatThrownBy(() -> userService.create(input2))
                .isInstanceOf(EntityAlreadyExistsException.class);
    }

    @Test
    @DisplayName("getUserById: должен вернуть пользователя по id")
    void getUserById_shouldReturnUser() {
        // given
        UserCreateInput input = new UserCreateInput(
                "testLogin",
                "test@mail.dev",
                "password123",
                UserRole.CLIENT
        );
        UserResponse created = userService.create(input);

        // when
        UserResponse response = userService.getUserById(created.id());

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(created.id());
        assertEquals(input.role(), response.role());
        assertEquals(input.login(), response.login());
        assertEquals(input.email(), response.email());
    }

    @Test
    @DisplayName("getUserById: должен выбросить исключение если пользователь не найден")
    void getUserById_shouldThrowExceptionWhenNotFound() {
        // given
        Long nonExistentId = 999L;

        // when & then
        assertThatThrownBy(() -> userService.getUserById(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("getUserByLogin: должен вернуть пользователя по login")
    void getUserByLogin_shouldReturnUser() {
        // given
        UserCreateInput input = new UserCreateInput(
                "testLogin",
                "test@mail.dev",
                "password123",
                UserRole.CLIENT
        );
        userService.create(input);

        // when
        UserResponse response = userService.getUserByLogin(input.login());

        // then
        assertThat(response).isNotNull();
        assertThat(response.role()).isEqualTo(input.role());
        assertThat(response.login()).isEqualTo(input.login());
        assertThat(response.email()).isEqualTo(input.email());
    }

    @Test
    @DisplayName("getUserByLogin: должен выбросить исключение если пользователь не найден")
    void getUserByLogin_shouldThrowExceptionWhenNotFound() {
        // given
        String nonExistentLogin = "nonExistent";

        // when & then
        assertThatThrownBy(() -> userService.getUserByLogin(nonExistentLogin))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("update: должен обновить все поля пользователя")
    void update_shouldUpdateAllFields() {
        // given
        UserCreateInput createInput = new UserCreateInput(
                "oldLogin",
                "old@mail.dev",
                "oldPassword",
                UserRole.CLIENT
        );
        UserResponse created = userService.create(createInput);

        UserUpdateInput updateInput = new UserUpdateInput(
                "newLogin",
                "new@mail.dev",
                "newPassword",
                UserRole.ADMIN
        );

        // when
        UserResponse response = userService.update(created.id(), updateInput);

        // then
        assertThat(response.id()).isEqualTo(created.id());
        assertThat(response.login()).isEqualTo(updateInput.login());
        assertThat(response.email()).isEqualTo(updateInput.email());
        assertThat(response.role()).isEqualTo(updateInput.role());

        User savedUser = userRepository.findById(created.id()).orElseThrow();
        assertThat(savedUser.getLogin()).isEqualTo(updateInput.login());
        assertThat(savedUser.getEmail()).isEqualTo(updateInput.email());
    }

    @Test
    @DisplayName("update: должен обновить только переданные поля")
    void update_shouldUpdateOnlyProvidedFields() {
        // given
        UserCreateInput createInput = new UserCreateInput(
                "testLogin",
                "test@mail.dev",
                "password123",
                UserRole.CLIENT
        );
        UserResponse created = userService.create(createInput);

        UserUpdateInput updateInput = new UserUpdateInput(
                null,
                "newemail@mail.dev",
                null,
                null
        );

        // when
        UserResponse response = userService.update(created.id(), updateInput);

        // then
        assertThat(response.id()).isEqualTo(created.id());
        // login не обновлялся → остался из createInput
        assertThat(response.login()).isEqualTo(createInput.login());
        // email обновился → стал из updateInput
        assertThat(response.email()).isEqualTo(updateInput.email());
        // role не обновлялась → осталась из createInput
        assertThat(response.role()).isEqualTo(createInput.role());

        User savedUser = userRepository.findById(created.id()).orElseThrow();
        assertThat(savedUser.getLogin()).isEqualTo(createInput.login());
        assertThat(savedUser.getEmail()).isEqualTo(updateInput.email());
    }

    @Test
    @DisplayName("update: должен выбросить исключение при попытке обновить на существующие email и login")
    void update_shouldThrowExceptionWhenEmailOrLoginExists() {
        // given
        UserCreateInput input1 = new UserCreateInput(
                "user1",
                "user1@mail.dev",
                "password123",
                UserRole.CLIENT
        );
        userService.create(input1);

        UserCreateInput input2 = new UserCreateInput(
                "user2",
                "user2@mail.dev",
                "password123",
                UserRole.CLIENT
        );
        UserResponse created2 = userService.create(input2);

        // Пытаемся обновить user2 на данные user1 → конфликт
        UserUpdateInput updateInput = new UserUpdateInput(
                input1.login(),
                input1.email(),
                null,
                null
        );

        // when & then
        assertThatThrownBy(() -> userService.update(created2.id(), updateInput))
                .isInstanceOf(EntityAlreadyExistsException.class);
    }

    @Test
    @DisplayName("update: должен выбросить исключение если пользователь не найден")
    void update_shouldThrowExceptionWhenNotFound() {
        // given
        Long nonExistentId = 999L;
        UserUpdateInput updateInput = new UserUpdateInput(
                "newLogin",
                "new@mail.dev",
                "newPassword",
                UserRole.ADMIN
        );

        // when & then
        assertThatThrownBy(() -> userService.update(nonExistentId, updateInput))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("delete: должен удалить пользователя")
    void delete_shouldDeleteUser() {
        // given
        UserCreateInput input = new UserCreateInput(
                "testLogin",
                "test@mail.dev",
                "password123",
                UserRole.CLIENT
        );
        UserResponse created = userService.create(input);

        assertThat(userRepository.findById(created.id())).isPresent();

        // when
        userService.delete(created.id());

        // then
        assertThat(userRepository.findById(created.id())).isEmpty();
    }

    @Test
    @DisplayName("delete: должен корректно обработать удаление несуществующего пользователя")
    void delete_shouldHandleNonExistentUser() {
        // given
        Long nonExistentId = 999L;

        // when
        userService.delete(nonExistentId);

        // then
        assertThat(userRepository.findById(nonExistentId)).isEmpty();
    }
}