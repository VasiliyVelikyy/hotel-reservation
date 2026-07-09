package ru.moskalev.hotel_reservation.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hotel_seq")
    @SequenceGenerator(name = "hotel_seq", sequenceName = "hotel_seq")
    private Long id;

    private String name;

    private String description;

    private String title;

    private String city;

    private String address;

    private Integer distance;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Room> rooms = new ArrayList<>();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "rating", column = @Column(name = "current_rating")),
            @AttributeOverride(name = "totalRating", column = @Column(name = "total_rating_sum")),
            @AttributeOverride(name = "numberOfRating", column = @Column(name = "rating_count"))
    })
    private Grade grade;
}
