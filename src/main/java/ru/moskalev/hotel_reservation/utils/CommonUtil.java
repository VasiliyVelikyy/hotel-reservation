package ru.moskalev.hotel_reservation.utils;

import org.springframework.data.domain.Sort;
import ru.moskalev.hotel_reservation.exception.PaginatedException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.function.Consumer;

import static ru.moskalev.hotel_reservation.Constants.DEFAULT_DIRECTION_ASC;
import static ru.moskalev.hotel_reservation.Constants.DIRECTION_DESC;
import static ru.moskalev.hotel_reservation.exception.ErrorMessagesTemplates.NOT_VALID_SORTED_TEMPLATE;

public class CommonUtil {
    public static Sort getSort(String sortBy, String direction) {
        return switch (direction.toUpperCase()) {
            case DEFAULT_DIRECTION_ASC -> Sort.by(sortBy).ascending();
            case DIRECTION_DESC -> Sort.by(sortBy).descending();
            default -> throw new PaginatedException(NOT_VALID_SORTED_TEMPLATE);
        };
    }

    /**
     * Обновляет поле сущности только если новое значение не null.
     *
     * @param newValue новое значение (может быть null)
     * @param setter   метод-сеттер для установки значения
     * @param <T>      тип значения
     */
    public static <T> void updateIfNotNull(T newValue, Consumer<T> setter) {
        if (newValue != null) {
            setter.accept(newValue);
        }
    }

    public static long toEpochSecond(LocalDate date) {
        return date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
    }
}
