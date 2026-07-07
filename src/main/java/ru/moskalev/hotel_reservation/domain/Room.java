package ru.moskalev.hotel_reservation.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    public static final String HOTEL_ID = "hotel_id";
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "room_seq")
    @SequenceGenerator(name = "room_seq", sequenceName = "room_seq")
    private Long id;

    private String name;

    private String description;

    private short number;

    private BigDecimal price;

    private byte maxCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = HOTEL_ID, nullable = false)
    private Hotel hotel;

    @Column(name = HOTEL_ID, insertable = false, updatable = false)
    private Long hotelId;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();
}


