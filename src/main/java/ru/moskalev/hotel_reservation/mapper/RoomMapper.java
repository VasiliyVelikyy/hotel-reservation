package ru.moskalev.hotel_reservation.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.moskalev.hotel_reservation.domain.Room;
import ru.moskalev.hotel_reservation.dto.room.RoomCreateInput;
import ru.moskalev.hotel_reservation.dto.room.RoomResponse;
import ru.moskalev.hotel_reservation.dto.room.RoomUpdateInput;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hotel", source = "hotel")
    @Mapping(target = "hotel.id", source = "hotel")
    Room toEntity(RoomCreateInput input, Long hotel);

    @Mapping(target = "hotelId", source = "hotel.id")
    RoomResponse toOutputDto(Room room);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hotel", ignore = true)
    @Mapping(target = "hotelId", ignore = true)
    Room updateEntity(RoomUpdateInput input, @MappingTarget Room room);

    List<RoomResponse> toOutputDtoList(List<Room> rooms);
}