package ru.moskalev.hotel_reservation.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.moskalev.hotel_reservation.domain.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Page<Room> findByHotelId(Long hotelId, Pageable pageable);
}
