package ru.moskalev.hotel_reservation.repo;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import ru.moskalev.hotel_reservation.domain.Room;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long>,
        JpaSpecificationExecutor<Room> {
    Page<Room> findByHotelId(Long hotelId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.id = :id")
    Optional<Room> findByIdForUpdate(Long id);
}
