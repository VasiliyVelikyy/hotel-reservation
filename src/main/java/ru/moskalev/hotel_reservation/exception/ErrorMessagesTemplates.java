package ru.moskalev.hotel_reservation.exception;

public class ErrorMessagesTemplates {
    public static final String HOTEL_NOT_FOUND_TEMPLATE ="Hotel whit id '%s' not found";
    public static final String NOT_VALID_SORTED_TEMPLATE="Not valid strategy of sorting";

    public static final String ROOM_NOT_FOUND_TEMPLATE = "Room with id %d not found";
    public static final String ROOM_NOT_BELONG_TO_HOTEL_TEMPLATE =
            "Room with id %d does not belong to hotel with id %d";
}
