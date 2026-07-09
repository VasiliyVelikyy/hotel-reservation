package ru.moskalev.hotel_reservation.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Grade {
    private Double rating;
    private Double totalRating;
    private Integer numberOfRating;
}
