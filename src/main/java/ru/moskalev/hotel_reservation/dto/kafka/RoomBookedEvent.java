package ru.moskalev.hotel_reservation.dto.kafka;

import java.time.LocalDate;

import static ru.moskalev.hotel_reservation.enumeration.KafkaEvent.ROOM_BOOKED;

public record RoomBookedEvent(Long userId, LocalDate checkInDate, LocalDate checkOutDate) implements BaseUserEvent {
    @Override
    public String getEventType() { return ROOM_BOOKED.name(); }

}