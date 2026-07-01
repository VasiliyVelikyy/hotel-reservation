package ru.moskalev.hotel_reservation.integration;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.moskalev.hotel_reservation.dto.HotelCreateInput;
import ru.moskalev.hotel_reservation.dto.HotelResponse;
import ru.moskalev.hotel_reservation.dto.HotelUpdateInput;
import ru.moskalev.hotel_reservation.integration.api.HotelApi;
import ru.moskalev.hotel_reservation.service.HotelService;

import static ru.moskalev.hotel_reservation.Constants.*;

@RestController
@AllArgsConstructor
@RequestMapping(V1 + HOTEL)
public class HotelController implements HotelApi {
    private final HotelService hotelService;

    @PostMapping
    public HotelResponse create(@RequestBody HotelCreateInput input) {
        return hotelService.create(input);
    }

    @GetMapping(HOTEL_ID)
    public HotelResponse getById(@PathVariable Long hotelId) {
        return hotelService.getById(hotelId);
    }

    @PutMapping(HOTEL_ID)
    public HotelResponse update(@PathVariable Long hotelId,
                                @RequestBody HotelUpdateInput input) {
        return hotelService.update(hotelId, input);
    }

    @DeleteMapping(HOTEL_ID)
    public void delete(@PathVariable Long hotelId) {
        hotelService.delete(hotelId);
    }

}
