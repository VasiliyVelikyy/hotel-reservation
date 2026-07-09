package ru.moskalev.hotel_reservation.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "statistics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatEventDocument {
    @Id
    private String id;
    private String eventType;
    private Long userId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private LocalDateTime createdAt;
}
