package ru.moskalev.hotel_reservation.integration.rest;


import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.moskalev.hotel_reservation.dto.room.RoomCreateInput;
import ru.moskalev.hotel_reservation.dto.room.RoomFilter;
import ru.moskalev.hotel_reservation.dto.room.RoomResponse;
import ru.moskalev.hotel_reservation.dto.room.RoomUpdateInput;
import ru.moskalev.hotel_reservation.integration.api.RoomApi;
import ru.moskalev.hotel_reservation.service.RoomService;

import static ru.moskalev.hotel_reservation.Constants.*;
import static ru.moskalev.hotel_reservation.utils.CommonUtil.getSort;

@RestController
@AllArgsConstructor
@RequestMapping(V1 + ROOM)
public class RoomController implements RoomApi {
    private final RoomService service;

    @PostMapping(HOTEL_ID)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public RoomResponse create(@PathVariable Long hotelId,
                               @RequestBody RoomCreateInput input) {
        return service.create(hotelId, input);
    }

    @GetMapping(ROOM_ID)
    @PreAuthorize("isAuthenticated()")
    public RoomResponse getById(@PathVariable Long roomId) {
        return service.getById(roomId);
    }

    @GetMapping(HOTEL + HOTEL_ID)
    @PreAuthorize("isAuthenticated()")
    public Page<@NonNull RoomResponse> getAllByHotelId(@PathVariable Long hotelId,
                                                       @RequestParam(defaultValue = DEFAULT_PAGE) int page,
                                                       @RequestParam(defaultValue = DEFAULT_SIZE) int size,
                                                       @RequestParam(defaultValue = DEFAULT_SORTED_BY_ID) String sortBy,
                                                       @RequestParam(defaultValue = DEFAULT_DIRECTION_ASC) String direction) {
        var sort = getSort(sortBy, direction);
        Pageable pageable = PageRequest.of(page, size, sort);
        return service.getAllByHotelId(hotelId, pageable);
    }

    @PostMapping(HOTEL + HOTEL_ID + FILTER)
    @PreAuthorize("isAuthenticated()")
    public Page<@NonNull RoomResponse> getAllByHotelIdAndFilter(@PathVariable Long hotelId,
                                                       @RequestParam(defaultValue = DEFAULT_PAGE) int page,
                                                       @RequestParam(defaultValue = DEFAULT_SIZE) int size,
                                                       @RequestParam(defaultValue = DEFAULT_SORTED_BY_ID) String sortBy,
                                                       @RequestParam(defaultValue = DEFAULT_DIRECTION_ASC) String direction,
                                                       @RequestBody(required = false) RoomFilter filter) {
        var sort = getSort(sortBy, direction);
        Pageable pageable = PageRequest.of(page, size, sort);
        return service.getAllByHotelIdAndFilter(hotelId, filter, pageable);
    }

    @PutMapping(ROOM_ID)
    @PreAuthorize("hasRole('ADMIN')")
    public RoomResponse update(@PathVariable Long roomId,
                               @RequestBody RoomUpdateInput input) {
        return service.update(roomId, input);
    }

    @DeleteMapping(ROOM_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long roomId) {
        service.delete(roomId);
    }
}