package ru.moskalev.hotel_reservation.exception;

public class BookingException extends RuntimeException {
    public BookingException(String message) {
        super(message);
    }
}
