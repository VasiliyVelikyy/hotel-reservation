package ru.moskalev.hotel_reservation.integration.rest;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.moskalev.hotel_reservation.dto.user.UserCreateInput;
import ru.moskalev.hotel_reservation.dto.user.UserResponse;
import ru.moskalev.hotel_reservation.dto.user.UserUpdateInput;
import ru.moskalev.hotel_reservation.integration.api.UserApi;
import ru.moskalev.hotel_reservation.service.UserService;

import static ru.moskalev.hotel_reservation.Constants.*;

@RestController
@AllArgsConstructor
@RequestMapping(V1 + USER)
public class UserController implements UserApi {
    private final UserService userService;

    @GetMapping(LOGIN + LOGIN_PATH)
    public UserResponse getUserByLogin(@PathVariable String login) {
        return userService.getUserByLogin(login);
    }

    @GetMapping(USER_ID)
    public UserResponse getUserById(@PathVariable Long userId) {
        return userService.getUserById(userId);
    }

    @PostMapping()
    public UserResponse create(@RequestBody UserCreateInput input) {
        return userService.create(input);
    }

    @PutMapping(USER_ID)
    public UserResponse update(@PathVariable Long userId, @RequestBody UserUpdateInput input) {
        return userService.update(userId, input);
    }

    @DeleteMapping(USER_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long userId) {
        userService.delete(userId);
    }
}
