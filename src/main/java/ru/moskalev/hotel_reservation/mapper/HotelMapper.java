package ru.moskalev.hotel_reservation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.moskalev.hotel_reservation.domain.Hotel;
import ru.moskalev.hotel_reservation.dto.hotel.HotelCreateInput;
import ru.moskalev.hotel_reservation.dto.hotel.HotelResponse;
import ru.moskalev.hotel_reservation.dto.hotel.HotelUpdateInput;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HotelMapper {

    @Mapping(target = "id", ignore = true)
    //@Mapping(target = "grades", ignore = true)
    Hotel toEntity(HotelCreateInput input);

    HotelResponse toOutputDto(Hotel hotel);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
   // @Mapping(target = "grades", ignore = true)
    Hotel updateEntity(HotelUpdateInput input, @MappingTarget Hotel hotel);

    List<HotelResponse> toOutputDtoList(List<Hotel> hotels);
}