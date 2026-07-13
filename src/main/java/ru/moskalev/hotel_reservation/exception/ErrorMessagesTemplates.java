package ru.moskalev.hotel_reservation.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorMessagesTemplates {
    public static final String HOTEL_NOT_FOUND_TEMPLATE = "Hotel whit id '%s' not found";
    public static final String ROOM_NOT_FOUND_TEMPLATE = "Room with id %s not found";
    public static final String USER_NOT_FOUND_TEMPLATE = "User with %s %s not found";
    public static final String BOOKING_NOT_FOUND_TEMPLATE = "Booking with id %s not found";
    public static final String NOT_VALID_SORTED_TEMPLATE = "Not valid strategy of sorting";
    public static final String USER_ALREADY_EXIST_TEMPLATE = "User with email %s and login %s already exist";

    public static final String BOOKING_TOO_MANY_GUESTS_TEMPLATE = "Too many guests, %s";
    public static final String BOOKING_START_DATE_NOT_VALID_TEMPLATE = "Start date must be before end date";
    public static final String ROOM_IS_ALREADY_BOOKED_FOR_THESE_DATES_TEMPLATE = "Room is already booked for these dates";
    public static final String YOURSELF_CANCEL_TEMPLATE = "You can only cancel your own bookings";

}
