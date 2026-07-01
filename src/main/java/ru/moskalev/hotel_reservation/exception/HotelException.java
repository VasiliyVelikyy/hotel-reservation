package ru.moskalev.hotel_reservation.exception;

public class HotelException extends RuntimeException {
    public static final String NOT_FOUND_EXCEPTION_TEMPLATE="Hotel whit id '%s' not found";
    public HotelException(String message) {
        super(message);
    }
}
