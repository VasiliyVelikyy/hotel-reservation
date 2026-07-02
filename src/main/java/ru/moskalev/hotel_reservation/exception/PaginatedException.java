package ru.moskalev.hotel_reservation.exception;

import jakarta.validation.ValidationException;

public class PaginatedException extends ValidationException {
    public PaginatedException(String message) {
        super(message);
    }
}
