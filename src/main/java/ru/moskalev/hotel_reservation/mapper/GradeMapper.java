package ru.moskalev.hotel_reservation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.moskalev.hotel_reservation.domain.Grade;
import ru.moskalev.hotel_reservation.dto.grade.GradeResponse;

@Mapper(componentModel = "spring")
public interface GradeMapper {

    @Mapping(target = "hotelId", source = "hotelId")
    GradeResponse toResponse(Grade grade, Long hotelId);
}
