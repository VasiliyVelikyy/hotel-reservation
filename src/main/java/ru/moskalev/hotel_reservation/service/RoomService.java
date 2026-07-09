package ru.moskalev.hotel_reservation.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.moskalev.hotel_reservation.domain.Room;
import ru.moskalev.hotel_reservation.dto.room.RoomCreateInput;
import ru.moskalev.hotel_reservation.dto.room.RoomFilter;
import ru.moskalev.hotel_reservation.dto.room.RoomResponse;
import ru.moskalev.hotel_reservation.dto.room.RoomUpdateInput;
import ru.moskalev.hotel_reservation.exception.EntityNotFoundException;
import ru.moskalev.hotel_reservation.mapper.RoomMapper;
import ru.moskalev.hotel_reservation.repo.RoomRepository;
import ru.moskalev.hotel_reservation.specification.RoomSpecification;

import static ru.moskalev.hotel_reservation.exception.ErrorMessagesTemplates.ROOM_NOT_FOUND_TEMPLATE;

@Service
@AllArgsConstructor
public class RoomService {
    private final HotelService hotelService;
    private final RoomRepository repository;
    private final RoomMapper mapper;

    @Transactional
    public RoomResponse create(Long hotelId, RoomCreateInput input) {
        hotelService.checkExistHotel(hotelId);
        Room savedRoom = repository.save(mapper.toEntity(input, hotelId));
        return mapper.toResponse(savedRoom);
    }

    @Transactional(readOnly = true)
    public RoomResponse getById(Long id) {
        Room room = getRoom(id);
        return mapper.toResponse(room);
    }

    @Transactional(readOnly = true)
    public Page<RoomResponse> getAllByHotelId(Long hotelId, Pageable pageable) {
        hotelService.checkExistHotel(hotelId);
        Page<Room> roomPage = repository.findByHotelId(hotelId, pageable);
        return roomPage.map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<RoomResponse> getAllByHotelIdAndFilter(Long hotelId, RoomFilter filter, Pageable pageable) {
        hotelService.checkExistHotel(hotelId);

        Specification<Room> spec = RoomSpecification.withFilter(filter);

        Specification<Room> hotelSpec = (root, query, cb) ->
                cb.equal(root.get("hotelId"), hotelId);

        Specification<Room> combinedSpec = spec.and(hotelSpec);

        Page<Room> roomPage = repository.findAll(combinedSpec, pageable);
        return roomPage.map(mapper::toResponse);
    }
    @Transactional
    public RoomResponse update(Long roomId, RoomUpdateInput input) {
        Room room = getRoom(roomId);
        Room updatedRoom = mapper.updateEntity(input, room);
        return mapper.toResponse(repository.save(updatedRoom));
    }

    @Transactional
    public void delete(Long roomId) {
        if (!repository.existsById(roomId)) {
            throw new EntityNotFoundException(ROOM_NOT_FOUND_TEMPLATE.formatted(roomId));
        }
        repository.deleteById(roomId);
    }

    public Room getRoom(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ROOM_NOT_FOUND_TEMPLATE.formatted(id)));
    }

    public Room findByIdForUpdate(Long roomId) {
       return repository.findByIdForUpdate(roomId)
                .orElseThrow(() -> new EntityNotFoundException(ROOM_NOT_FOUND_TEMPLATE.formatted(roomId)));
    }
}
