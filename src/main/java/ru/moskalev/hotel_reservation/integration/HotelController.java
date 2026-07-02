package ru.moskalev.hotel_reservation.integration;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.moskalev.hotel_reservation.dto.hotel.HotelCreateInput;
import ru.moskalev.hotel_reservation.dto.hotel.HotelResponse;
import ru.moskalev.hotel_reservation.dto.hotel.HotelUpdateInput;
import ru.moskalev.hotel_reservation.integration.api.HotelApi;
import ru.moskalev.hotel_reservation.service.HotelService;

import static ru.moskalev.hotel_reservation.Constants.*;

@RestController
@AllArgsConstructor
@RequestMapping(V1 + HOTEL)
public class HotelController implements HotelApi {
    private final HotelService hotelService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HotelResponse create(@RequestBody HotelCreateInput input) {
        return hotelService.create(input);
    }

    @GetMapping(HOTEL_ID)
    public HotelResponse getById(@PathVariable Long hotelId) {
        return hotelService.getById(hotelId);
    }

    @GetMapping
    public Page<HotelResponse> getAll(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size,
                                      @RequestParam(defaultValue = "id") String sortBy,
                                      @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return hotelService.getAll(pageable);
    }

    @PutMapping(HOTEL_ID)
    public HotelResponse update(@PathVariable Long hotelId,
                                @RequestBody HotelUpdateInput input) {
        return hotelService.update(hotelId, input);
    }

    @DeleteMapping(HOTEL_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long hotelId) {
        hotelService.delete(hotelId);
    }

}
