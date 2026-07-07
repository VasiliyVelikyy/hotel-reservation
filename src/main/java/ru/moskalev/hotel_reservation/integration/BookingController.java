package ru.moskalev.hotel_reservation.integration;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.moskalev.hotel_reservation.dto.booking.BookingCreateRequest;
import ru.moskalev.hotel_reservation.dto.booking.BookingResponse;
import ru.moskalev.hotel_reservation.integration.api.BookingApi;
import ru.moskalev.hotel_reservation.service.BookingService;

import java.util.List;

import static ru.moskalev.hotel_reservation.Constants.*;

@RestController
@AllArgsConstructor
@RequestMapping(V1 + BOOKING)
public class BookingController implements BookingApi {
    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@RequestBody BookingCreateRequest input) {
        return bookingService.create(input);
    }

    @GetMapping(BOOKING_ID)
    public BookingResponse getById(@PathVariable Long bookingId) {
        return bookingService.getById(bookingId);
    }

    @GetMapping("/my")
    public List<BookingResponse> getMyBookings() {
        return bookingService.getMyBookings();
    }

    @DeleteMapping(BOOKING_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long bookingId) {
        bookingService.cancel(bookingId);
    }

}
