package ru.moskalev.hotel_reservation.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.moskalev.hotel_reservation.dto.user.UserCreateInput;
import ru.moskalev.hotel_reservation.dto.user.UserResponse;
import ru.moskalev.hotel_reservation.dto.user.UserUpdateInput;
import ru.moskalev.hotel_reservation.enumeration.UserRole;
import ru.moskalev.hotel_reservation.exception.EntityNotFoundException;
import ru.moskalev.hotel_reservation.integration.rest.UserController;
import ru.moskalev.hotel_reservation.service.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.moskalev.hotel_reservation.Constants.USER;
import static ru.moskalev.hotel_reservation.Constants.V1;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @MockitoBean
    private UserService userService;

    private static final String BASE_URL = V1 + USER;
    private static final Long USER_ID_VALUE = 1L;

    @Test
    @DisplayName("POST /user — 200 — пользователь успешно создан")
    void create_success() throws Exception {
        UserCreateInput input = new UserCreateInput(
                "testLogin",
                "test@mail.dev",
                "qwertyui",
                UserRole.CLIENT
        );

        UserResponse expected = new UserResponse(
                1L,
                "testLogin",
                "test@mail.dev",
                UserRole.CLIENT
        );

        when(userService.create(any(UserCreateInput.class))).thenReturn(expected);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.login").value("testLogin"))
                .andExpect(jsonPath("$.email").value("test@mail.dev"))
                .andExpect(jsonPath("$.role").value("CLIENT"));

        verify(userService).create(any(UserCreateInput.class));
    }

    @Test
    @DisplayName("POST /user — 400 — пустой login")
    void create_badRequest_emptyLogin() throws Exception {
        UserCreateInput invalidInput = new UserCreateInput(
                "",
                "test@mail.dev",
                "qwertyui",
                UserRole.CLIENT
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInput)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any(UserCreateInput.class));
    }

    @Test
    @DisplayName("POST /user — 400 — login слишком короткий (меньше 3 символов)")
    void create_badRequest_loginTooShort() throws Exception {
        UserCreateInput invalidInput = new UserCreateInput(
                "ab",
                "test@mail.dev",
                "qwertyui",
                UserRole.CLIENT
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInput)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any(UserCreateInput.class));
    }

    @Test
    @DisplayName("POST /user — 400 — некорректный формат email")
    void create_badRequest_invalidEmail() throws Exception {
        UserCreateInput invalidInput = new UserCreateInput(
                "testLogin",
                "not-an-email",
                "qwertyui",
                UserRole.CLIENT
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInput)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any(UserCreateInput.class));
    }

    @Test
    @DisplayName("POST /user — 400 — пароль слишком короткий (меньше 8 символов)")
    void create_badRequest_passwordTooShort() throws Exception {
        UserCreateInput invalidInput = new UserCreateInput(
                "testLogin",
                "test@mail.dev",
                "short",
                UserRole.CLIENT
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInput)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any(UserCreateInput.class));
    }

    @Test
    @DisplayName("POST /user — 400 — роль равна null")
    void create_badRequest_nullRole() throws Exception {
        UserCreateInput invalidInput = new UserCreateInput(
                "testLogin",
                "test@mail.dev",
                "qwertyui",
                null
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInput)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any(UserCreateInput.class));
    }

    @Test
    @DisplayName("GET /user/{userId} — 200 — пользователь найден")
    void getUserById_success() throws Exception {
        UserResponse expected = new UserResponse(
                USER_ID_VALUE,
                "testLogin",
                "test@mail.dev",
                UserRole.CLIENT
        );

        when(userService.getUserById(USER_ID_VALUE)).thenReturn(expected);

        mockMvc.perform(get(BASE_URL + "/{userId}", USER_ID_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID_VALUE))
                .andExpect(jsonPath("$.login").value("testLogin"))
                .andExpect(jsonPath("$.email").value("test@mail.dev"))
                .andExpect(jsonPath("$.role").value("CLIENT"));

        verify(userService).getUserById(USER_ID_VALUE);
    }

    @Test
    @DisplayName("GET /user/{userId} — 404 — пользователь не найден")
    void getUserById_notFound() throws Exception {
        when(userService.getUserById(USER_ID_VALUE))
                .thenThrow(new EntityNotFoundException("User not found"));

        mockMvc.perform(get(BASE_URL + "/{userId}", USER_ID_VALUE))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(USER_ID_VALUE);
    }

    @Test
    @DisplayName("GET /user/login/{login} — 200 — пользователь найден по логину")
    void getUserByLogin_success() throws Exception {
        String login = "testLogin";
        UserResponse expected = new UserResponse(
                1L,
                login,
                "test@mail.dev",
                UserRole.CLIENT
        );

        when(userService.getUserByLogin(login)).thenReturn(expected);

        mockMvc.perform(get(BASE_URL + "/login/{login}", login))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.login").value(login))
                .andExpect(jsonPath("$.email").value("test@mail.dev"))
                .andExpect(jsonPath("$.role").value("CLIENT"));

        verify(userService).getUserByLogin(login);
    }

    @Test
    @DisplayName("GET /user/login/{login} — 404 — пользователь с таким логином не найден")
    void getUserByLogin_notFound() throws Exception {
        String login = "nonexistent";

        when(userService.getUserByLogin(login))
                .thenThrow(new EntityNotFoundException("User not found"));

        mockMvc.perform(get(BASE_URL + "/login/{login}", login))
                .andExpect(status().isNotFound());

        verify(userService).getUserByLogin(login);
    }

    @Test
    @DisplayName("PUT /user/{userId} — 200 — пользователь успешно обновлен")
    void update_success() throws Exception {
        UserUpdateInput input = new UserUpdateInput(
                "newLogin",
                "new@mail.dev",
                null,
                UserRole.ADMIN
        );

        UserResponse expected = new UserResponse(
                USER_ID_VALUE,
                "newLogin",
                "new@mail.dev",
                UserRole.ADMIN
        );

        when(userService.update(eq(USER_ID_VALUE), any(UserUpdateInput.class))).thenReturn(expected);

        mockMvc.perform(put(BASE_URL + "/{userId}", USER_ID_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID_VALUE))
                .andExpect(jsonPath("$.login").value("newLogin"))
                .andExpect(jsonPath("$.email").value("new@mail.dev"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        verify(userService).update(eq(USER_ID_VALUE), any(UserUpdateInput.class));
    }

    @Test
    @DisplayName("PUT /user/{userId} — 400 — некорректный email при обновлении")
    void update_badRequest_invalidEmail() throws Exception {
        UserUpdateInput invalidInput = new UserUpdateInput(
                "newLogin",
                "invalid-email",
                null,
                null
        );

        mockMvc.perform(put(BASE_URL + "/{userId}", USER_ID_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInput)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).update(anyLong(), any(UserUpdateInput.class));
    }

    @Test
    @DisplayName("PUT /user/{userId} — 400 — пароль слишком короткий при обновлении")
    void update_badRequest_passwordTooShort() throws Exception {
        UserUpdateInput invalidInput = new UserUpdateInput(
                "newLogin",
                null,
                "short",
                null
        );

        mockMvc.perform(put(BASE_URL + "/{userId}", USER_ID_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInput)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).update(anyLong(), any(UserUpdateInput.class));
    }

    @Test
    @DisplayName("PUT /user/{userId} — 404 — пользователь для обновления не найден")
    void update_notFound() throws Exception {
        UserUpdateInput input = new UserUpdateInput(
                "newLogin",
                null,
                null,
                null
        );

        when(userService.update(eq(USER_ID_VALUE), any(UserUpdateInput.class)))
                .thenThrow(new EntityNotFoundException("User not found"));

        mockMvc.perform(put(BASE_URL + "/{userId}", USER_ID_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound());

        verify(userService).update(eq(USER_ID_VALUE), any(UserUpdateInput.class));
    }

    @Test
    @DisplayName("DELETE /user/{userId} — 204 — пользователь успешно удален")
    void delete_success() throws Exception {
        doNothing().when(userService).delete(USER_ID_VALUE);

        mockMvc.perform(delete(BASE_URL + "/{userId}", USER_ID_VALUE))
                .andExpect(status().isNoContent());

        verify(userService).delete(USER_ID_VALUE);
    }

    @Test
    @DisplayName("DELETE /user/{userId} — 404 — пользователь для удаления не найден")
    void delete_notFound() throws Exception {
        doThrow(new EntityNotFoundException("User not found"))
                .when(userService).delete(USER_ID_VALUE);

        mockMvc.perform(delete(BASE_URL + "/{userId}", USER_ID_VALUE))
                .andExpect(status().isNotFound());

        verify(userService).delete(USER_ID_VALUE);
    }
}