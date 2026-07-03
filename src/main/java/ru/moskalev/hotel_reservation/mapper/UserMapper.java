package ru.moskalev.hotel_reservation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.moskalev.hotel_reservation.domain.User;
import ru.moskalev.hotel_reservation.dto.user.UserCreateInput;
import ru.moskalev.hotel_reservation.dto.user.UserResponse;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hashPassword", ignore = true)
    User toEntity(UserCreateInput input);

    UserResponse toOutputDto(User user);
}
