package ru.moskalev.hotel_reservation.utils;

import org.springframework.data.domain.Sort;
import ru.moskalev.hotel_reservation.exception.PaginatedException;

import static ru.moskalev.hotel_reservation.Constants.DEFAULT_DIRECTION_ASC;
import static ru.moskalev.hotel_reservation.Constants.DIRECTION_DESC;
import static ru.moskalev.hotel_reservation.exception.ErrorMessagesTemplates.NOT_VALID_SORTED_TEMPLATE;

public class CommonUtil {
    public static Sort getSort(String sortBy, String direction) {
        return switch (direction.toLowerCase()) {
            case DEFAULT_DIRECTION_ASC -> Sort.by(sortBy).ascending();
            case DIRECTION_DESC -> Sort.by(sortBy).descending();
            default -> throw new PaginatedException(NOT_VALID_SORTED_TEMPLATE);
        };
    }
}
