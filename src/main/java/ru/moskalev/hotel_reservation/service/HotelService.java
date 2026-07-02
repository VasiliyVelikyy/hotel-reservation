package ru.moskalev.hotel_reservation.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.moskalev.hotel_reservation.domain.Hotel;
import ru.moskalev.hotel_reservation.dto.hotel.HotelCreateInput;
import ru.moskalev.hotel_reservation.dto.hotel.HotelResponse;
import ru.moskalev.hotel_reservation.dto.hotel.HotelUpdateInput;
import ru.moskalev.hotel_reservation.mapper.HotelMapper;
import ru.moskalev.hotel_reservation.repo.HotelRepository;

import static ru.moskalev.hotel_reservation.exception.HotelException.NOT_FOUND_EXCEPTION_TEMPLATE;

@Service
@AllArgsConstructor
public class HotelService {
    private final HotelRepository repository;
    private final HotelMapper mapper;

    @Transactional
    public HotelResponse create(HotelCreateInput input) {
        var savedHotel = repository.save(mapper.toEntity(input));
        return mapper.toOutputDto(savedHotel);
    }

    @Transactional(readOnly = true)
    public HotelResponse getById(Long id) {
        var entity = getEntity(id);
        return mapper.toOutputDto(entity);
    }

    private Hotel getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_EXCEPTION_TEMPLATE.formatted(id)));
    }

    @Transactional
    public HotelResponse update(Long id, HotelUpdateInput input) {
        Hotel hotel = getEntity(id);
        var updateEntity = mapper.updateEntity(input, hotel);
        return mapper.toOutputDto(repository.save(updateEntity));
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<HotelResponse> getAll(Pageable pageable) {
        Page<Hotel> hotelPage = repository.findAll(pageable);
        return hotelPage.map(mapper::toOutputDto);
    }
}
