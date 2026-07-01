package ru.moskalev.hotel_reservation.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.moskalev.hotel_reservation.domain.Grade;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
}
