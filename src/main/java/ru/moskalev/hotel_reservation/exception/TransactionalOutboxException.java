package ru.moskalev.hotel_reservation.exception;

public class TransactionalOutboxException extends RuntimeException {
    public TransactionalOutboxException(String message, Throwable e) {
        super(message, e);
    }
}
