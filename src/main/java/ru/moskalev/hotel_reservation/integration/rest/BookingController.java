package ru.moskalev.hotel_reservation.integration.rest;

import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.moskalev.hotel_reservation.dto.booking.BookingCreateRequest;
import ru.moskalev.hotel_reservation.dto.booking.BookingResponse;
import ru.moskalev.hotel_reservation.integration.api.BookingApi;
import ru.moskalev.hotel_reservation.service.BookingService;

import java.util.List;

import static ru.moskalev.hotel_reservation.Constants.*;
import static ru.moskalev.hotel_reservation.utils.CommonUtil.getSort;

@RestController
@AllArgsConstructor
@RequestMapping(V1 + BOOKING)
public class BookingController implements BookingApi {
    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public BookingResponse create(@RequestBody BookingCreateRequest input) {
        return bookingService.create(input);
    }

    @GetMapping(BOOKING_ID)
    @PreAuthorize("isAuthenticated()")
    public BookingResponse getById(@PathVariable Long bookingId) {
        return bookingService.getById(bookingId);
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public List<BookingResponse> getMyBookings() {
        return bookingService.getMyBookings();
    }

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public Page<@NonNull BookingResponse> getAllBookings(@RequestParam(defaultValue = DEFAULT_PAGE) int page,
                                                         @RequestParam(defaultValue = DEFAULT_SIZE) int size,
                                                         @RequestParam(defaultValue = DEFAULT_SORTED_BY_ID) String sortBy,
                                                         @RequestParam(defaultValue = DEFAULT_DIRECTION_ASC) String direction) {
        var sort = getSort(sortBy, direction);
        Pageable pageable = PageRequest.of(page, size, sort);
        return bookingService.getAllBookings(pageable);
    }

    @DeleteMapping(BOOKING_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void cancel(@PathVariable Long bookingId) {
        bookingService.cancel(bookingId);
    }

}
