package ru.moskalev.hotel_reservation.dto.kafka;

public interface BaseUserEvent {
    String getEventType();

    Long userId();
}