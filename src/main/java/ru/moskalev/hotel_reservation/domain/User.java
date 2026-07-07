package ru.moskalev.hotel_reservation.domain;

import jakarta.persistence.*;
import lombok.*;
import ru.moskalev.hotel_reservation.enumeration.UserRole;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_seq")
    @SequenceGenerator(name = "users_seq", sequenceName = "users_seq")
    private Long id;

    private String login;

    private String email;

    private String hashPassword;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Booking> bookings =new ArrayList<>();
}
