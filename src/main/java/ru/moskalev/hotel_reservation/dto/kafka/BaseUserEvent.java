package ru.moskalev.hotel_reservation.dto.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface BaseUserEvent {
    @JsonIgnore
    String getEventType();

    Long userId();
}