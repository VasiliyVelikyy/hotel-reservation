package ru.moskalev.hotel_reservation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.moskalev.hotel_reservation.domain.Booking;
import ru.moskalev.hotel_reservation.dto.booking.BookingCreateRequest;
import ru.moskalev.hotel_reservation.dto.booking.BookingResponse;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(source = "room.id", target = "roomId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "startDate", target = "startDate", qualifiedByName = "longToLocalDate")
    @Mapping(source = "endDate", target = "endDate", qualifiedByName = "longToLocalDate")
    BookingResponse toResponse(Booking booking);

    @Named("longToLocalDate")
    default LocalDate longToLocalDate(long timestamp) {
        return Instant.ofEpochSecond(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    @Named("localDateToLong")
    default Long localDateToLong(LocalDate date) {
        return date.atStartOfDay(ZoneId.systemDefault())
                .toEpochSecond();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(source = "startDate", target = "startDate", qualifiedByName = "localDateToLong")
    @Mapping(source = "endDate", target = "endDate", qualifiedByName = "localDateToLong")
    Booking toEntity(BookingCreateRequest request);
}
