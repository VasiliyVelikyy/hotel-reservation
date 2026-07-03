package ru.moskalev.hotel_reservation.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.moskalev.hotel_reservation.enumeration.UserRole;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "room_seq")
    @SequenceGenerator(name = "room_seq", sequenceName = "room_seq")
    private Long id;

    private String login;

    private String email;

    private String hashPassword;

    @Enumerated(EnumType.STRING)
    private UserRole role;
}
