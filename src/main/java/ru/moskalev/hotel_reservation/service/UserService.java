package ru.moskalev.hotel_reservation.service;

import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.moskalev.hotel_reservation.domain.User;
import ru.moskalev.hotel_reservation.dto.kafka.UserRegisteredEvent;
import ru.moskalev.hotel_reservation.dto.user.UserCreateInput;
import ru.moskalev.hotel_reservation.dto.user.UserResponse;
import ru.moskalev.hotel_reservation.dto.user.UserUpdateInput;
import ru.moskalev.hotel_reservation.exception.EntityAlreadyExistsException;
import ru.moskalev.hotel_reservation.exception.EntityNotFoundException;
import ru.moskalev.hotel_reservation.integration.kafka.KafkaStatsPublisher;
import ru.moskalev.hotel_reservation.mapper.UserMapper;
import ru.moskalev.hotel_reservation.repo.UserRepository;

import static ru.moskalev.hotel_reservation.exception.ErrorMessagesTemplates.USER_ALREADY_EXIST_TEMPLATE;
import static ru.moskalev.hotel_reservation.exception.ErrorMessagesTemplates.USER_NOT_FOUND_TEMPLATE;
import static ru.moskalev.hotel_reservation.utils.CommonUtil.updateIfNotNull;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final KafkaStatsPublisher kafkaStatsPublisher;

    @Transactional(readOnly = true)
    public UserResponse getUserByLogin(String login) {
        var user = findUserByLogin(login);
        return mapper.toResponse(user);
    }

    protected User findUserByLogin(String login) {
        return repository.findUserByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_TEMPLATE.formatted("login", login)));
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        var user = repository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_TEMPLATE.formatted("id", userId)));
        return mapper.toResponse(user);
    }

    @Transactional
    public UserResponse create(UserCreateInput input) {
        if (repository.existsByEmailAndLogin(input.email(), input.login())) {
            throw new EntityAlreadyExistsException(
                    USER_ALREADY_EXIST_TEMPLATE.formatted(input.email(), input.login())
            );
        }
        var userWithPass = hashedPassUser(mapper.toEntity(input), input.password());
        var savedUser = repository.save(userWithPass);

        kafkaStatsPublisher.publishUserEventAfterCommit(new UserRegisteredEvent(savedUser.getId()));
        return mapper.toResponse(savedUser);
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateInput input) {
        User user = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        USER_NOT_FOUND_TEMPLATE.formatted("id", id)
                ));

        validateUniqueness(user, input);

        updateUserFields(user, input);

        var savedUser = repository.save(user);
        return mapper.toResponse(savedUser);
    }

    @Transactional
    public void delete(Long userId) {
        repository.deleteById(userId);
    }

    private void updateUserFields(User user, UserUpdateInput input) {
        updateIfNotNull(input.login(), user::setLogin);
        updateIfNotNull(input.email(), user::setEmail);
        updateIfNotNull(input.role(), user::setRole);

        if (input.password() != null) {
            user.setHashPassword(passwordEncoder.encode(input.password()));
        }
    }

    private void validateUniqueness(User user, UserUpdateInput input) {
        String newLogin = input.login() != null ? input.login() : user.getLogin();
        String newEmail = input.email() != null ? input.email() : user.getEmail();

        if (newLogin.equals(user.getLogin()) && newEmail.equals(user.getEmail())) {
            return;
        }

        if (repository.existsByEmailAndLoginExcludingId(newEmail, newLogin, user.getId())) {
            throw new EntityAlreadyExistsException(
                    USER_ALREADY_EXIST_TEMPLATE.formatted(newEmail, newLogin)
            );
        }
    }

    private User hashedPassUser(User entity, String rawPassword) {
        entity.setHashPassword(passwordEncoder.encode(rawPassword));
        return entity;
    }


    public User getReferenceById(Long userId) {
        return repository.getReferenceById(userId);
    }
}
