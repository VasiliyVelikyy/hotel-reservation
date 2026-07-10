package ru.moskalev.hotel_reservation.exception;

public class CsvGenerateException extends RuntimeException {
    public CsvGenerateException(String message, Throwable e) {
        super(message,e);
    }
}
