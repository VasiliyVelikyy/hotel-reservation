package ru.moskalev.hotel_reservation.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.moskalev.hotel_reservation.domain.StatEventDocument;
import ru.moskalev.hotel_reservation.dto.kafka.RoomBookedEvent;
import ru.moskalev.hotel_reservation.dto.kafka.UserRegisteredEvent;
import ru.moskalev.hotel_reservation.repo.StatEventRepository;

import java.time.LocalDateTime;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class StatsService {
    private final StatEventRepository repository;

    public void consumeUserRegister(UserRegisteredEvent event) {
        StatEventDocument document = StatEventDocument.builder()
                .eventType(event.getEventType())
                .userId(event.userId())
                .createdAt(LocalDateTime.now())
                .build();
        repository.save(document);
    }

    public void consumeRoomBooked(RoomBookedEvent event) {
        StatEventDocument doc = StatEventDocument.builder()
                .eventType(event.getEventType())
                .userId(event.userId())
                .checkInDate(event.checkInDate())
                .checkOutDate(event.checkOutDate())
                .createdAt(LocalDateTime.now())
                .build();
        repository.save(doc);
    }

    public Stream<StatEventDocument> findAll() {
        return repository.findAllBy();
    }
}
