package ru.moskalev.hotel_reservation.integration.kafka;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.moskalev.hotel_reservation.dto.kafka.RoomBookedEvent;
import ru.moskalev.hotel_reservation.dto.kafka.UserRegisteredEvent;
import ru.moskalev.hotel_reservation.service.StatsService;

@Service
@AllArgsConstructor
@Slf4j
public class StatsKafkaConsumer {
    private final StatsService statsService;

    @KafkaListener(
            topics = "${spring.kafka.topics.user}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeUserRegister(UserRegisteredEvent event) {
        try {
            log.info("Received user registration event: {}", event.toString());
            statsService.consumeUserRegister(event);
        } catch (Exception e) {
            log.error("Failed to deserialize UserRegisteredEvent", e);
        }
    }

    @KafkaListener(
            topics = "${spring.kafka.topics.booking}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeRoomBooked(RoomBookedEvent event) {
        try {
            log.info("Received booking event: {}", event.toString());
            statsService.consumeRoomBooked(event);
        } catch (Exception e) {
            log.error("Failed to deserialize RoomBookedEvent", e);
        }
    }
}