package ru.moskalev.hotel_reservation.integration.rest;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.moskalev.hotel_reservation.dto.grade.GradeResponse;
import ru.moskalev.hotel_reservation.dto.hotel.HotelCreateInput;
import ru.moskalev.hotel_reservation.dto.hotel.HotelFilter;
import ru.moskalev.hotel_reservation.dto.hotel.HotelResponse;
import ru.moskalev.hotel_reservation.dto.hotel.HotelUpdateInput;
import ru.moskalev.hotel_reservation.integration.api.HotelApi;
import ru.moskalev.hotel_reservation.service.HotelService;

import static ru.moskalev.hotel_reservation.Constants.*;
import static ru.moskalev.hotel_reservation.utils.CommonUtil.getSort;

//todo http file, logging,readme

@RestController
@AllArgsConstructor
@RequestMapping(V1 + HOTEL)
public class HotelController implements HotelApi {
    private final HotelService hotelService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public HotelResponse create(@RequestBody HotelCreateInput input) {
        return hotelService.create(input);
    }

    @GetMapping(HOTEL_ID)
    @PreAuthorize("isAuthenticated()")
    public HotelResponse getById(@PathVariable Long hotelId) {
        return hotelService.getById(hotelId);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Page<HotelResponse> getAll(@RequestParam(defaultValue = DEFAULT_PAGE) int page,
                                      @RequestParam(defaultValue = DEFAULT_SIZE) int size,
                                      @RequestParam(defaultValue = DEFAULT_SORTED_BY_ID) String sortBy,
                                      @RequestParam(defaultValue = DEFAULT_DIRECTION_ASC) String direction) {
        var sort = getSort(sortBy, direction);
        Pageable pageable = PageRequest.of(page, size, sort);
        return hotelService.getAll(pageable);
    }

    @PostMapping(FILTER)
    @PreAuthorize("isAuthenticated()")
    public Page<HotelResponse> getAllByFilter(@RequestParam(defaultValue = DEFAULT_PAGE) int page,
                                              @RequestParam(defaultValue = DEFAULT_SIZE) int size,
                                              @RequestParam(defaultValue = DEFAULT_SORTED_BY_ID) String sortBy,
                                              @RequestParam(defaultValue = DEFAULT_DIRECTION_ASC) String direction,
                                              @RequestBody(required = false) HotelFilter filter) {
        var sort = getSort(sortBy, direction);
        Pageable pageable = PageRequest.of(page, size, sort);
        return hotelService.getAllByFilter(filter, pageable);
    }

    @PutMapping(HOTEL_ID)
    @PreAuthorize("hasRole('ADMIN')")
    public HotelResponse update(@PathVariable Long hotelId,
                                @RequestBody HotelUpdateInput input) {
        return hotelService.update(hotelId, input);
    }

    @DeleteMapping(HOTEL_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long hotelId) {
        hotelService.delete(hotelId);
    }

    @PostMapping(HOTEL_ID + GRADE)
    @PreAuthorize("isAuthenticated()")
    public GradeResponse rate(@PathVariable Long hotelId,
                              @RequestParam @Min(1) @Max(5) @NotNull Byte newMark) {
        return hotelService.rate(hotelId, newMark);
    }

}
