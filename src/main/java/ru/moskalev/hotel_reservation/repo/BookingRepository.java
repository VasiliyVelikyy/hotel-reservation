package ru.moskalev.hotel_reservation.repo;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.moskalev.hotel_reservation.domain.Booking;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
            "FROM Booking b WHERE b.room.id = :roomId " +
            "AND b.startDate < :reqEnd AND b.endDate > :reqStart")
    boolean existsOverlappingBooking(Long roomId,
                                     long reqStart,
                                     long reqEnd);

    @EntityGraph(attributePaths = {"room", "user"})
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId ORDER BY b.startDate DESC")
    List<Booking> findByUserId(Long userId);


    Optional<Booking> findByIdAndUserId(Long id, Long userId);
}
