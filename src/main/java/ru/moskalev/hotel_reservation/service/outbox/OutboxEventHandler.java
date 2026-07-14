package ru.moskalev.hotel_reservation.service.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.moskalev.hotel_reservation.dto.kafka.OutboxEvent;
import ru.moskalev.hotel_reservation.dto.kafka.RoomBookedEvent;
import ru.moskalev.hotel_reservation.dto.kafka.UserRegisteredEvent;
import ru.moskalev.hotel_reservation.integration.kafka.KafkaStatsPublisher;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventHandler {
    private final EventOutboxService eventOutboxService;
    private final KafkaStatsPublisher publisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void processSingleEvent(OutboxEvent event) {
        var eventType = event.getEventType();

        switch (eventType) {
            case "USER_REGISTERED" -> {
                UserRegisteredEvent userEvent = eventOutboxService.deserializePayload(
                        event.getPayload(),
                        UserRegisteredEvent.class
                );
                publisher.publishUserEvent(userEvent);
            }
            case "ROOM_BOOKED" -> {
                RoomBookedEvent bookedEvent = eventOutboxService.deserializePayload(
                        event.getPayload(),
                        RoomBookedEvent.class
                );
                publisher.publishBookingEvent(bookedEvent);
            }
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        }
        eventOutboxService.delete(event);
    }
}
