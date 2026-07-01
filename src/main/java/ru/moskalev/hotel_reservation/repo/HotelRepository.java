package ru.moskalev.hotel_reservation.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.moskalev.hotel_reservation.domain.Hotel;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
}
