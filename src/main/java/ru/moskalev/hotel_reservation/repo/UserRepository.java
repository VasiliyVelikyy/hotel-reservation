package ru.moskalev.hotel_reservation.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.moskalev.hotel_reservation.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByLogin(String login);

    boolean existsByEmailAndLogin(String email, String login);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
            "FROM User u " +
            "WHERE u.email = :email AND u.login = :login AND u.id <> :excludeId")
    boolean existsByEmailAndLoginExcludingId(String email,
                                             String login,
                                             Long excludeId
    );
}

