package ru.moskalev.hotel_reservation.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

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

    private long freeStartDate;

    private long freeEndDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = HOTEL_ID, nullable = false)
    private Hotel hotel;

    @Column(name = HOTEL_ID, insertable = false, updatable = false)
    private Long hotelId;
}
