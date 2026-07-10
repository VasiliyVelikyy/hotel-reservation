package ru.moskalev.hotel_reservation.dto.kafka;

import static ru.moskalev.hotel_reservation.enumeration.KafkaEvent.USER_REGISTERED;

public record UserRegisteredEvent(Long userId) implements BaseUserEvent {
    @Override
    public String getEventType() { return USER_REGISTERED.name(); }
}
