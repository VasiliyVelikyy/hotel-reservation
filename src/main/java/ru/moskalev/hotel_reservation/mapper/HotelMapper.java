package ru.moskalev.hotel_reservation.mapper;

import org.mapstruct.*;
import ru.moskalev.hotel_reservation.domain.Grade;
import ru.moskalev.hotel_reservation.domain.Hotel;
import ru.moskalev.hotel_reservation.dto.hotel.HotelCreateInput;
import ru.moskalev.hotel_reservation.dto.hotel.HotelResponse;
import ru.moskalev.hotel_reservation.dto.hotel.HotelUpdateInput;

@Mapper(componentModel = "spring")
public interface HotelMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "grade", expression = "java(initialGrade())")
    Hotel toEntity(HotelCreateInput input);

    @Mapping(source = "grade.rating", target = "rating")
    @Mapping(source = "grade.totalRating", target = "totalRating")
    @Mapping(source = "grade.numberOfRating", target = "numberOfRating")
    HotelResponse toResponse(Hotel hotel);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "grade", ignore = true)
    Hotel updateEntity(HotelUpdateInput input, @MappingTarget Hotel hotel);

    default Grade initialGrade(){
        return new Grade(0.0, 0.0, 0);
    }
}