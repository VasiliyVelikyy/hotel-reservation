package ru.moskalev.hotel_reservation.exception;

public class KafkaStatsException extends RuntimeException {
    public KafkaStatsException(String message,Throwable e) {
        super(message,e);
    }
}
