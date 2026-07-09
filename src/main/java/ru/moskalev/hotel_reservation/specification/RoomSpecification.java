package ru.moskalev.hotel_reservation.specification;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import ru.moskalev.hotel_reservation.domain.Booking;
import ru.moskalev.hotel_reservation.domain.Room;
import ru.moskalev.hotel_reservation.dto.room.RoomFilter;

import java.util.ArrayList;
import java.util.List;

public class RoomSpecification {

    public static Specification<Room> withFilter(RoomFilter filter) {
        return (Root<Room> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            if (filter.roomId() != null) {
                predicates.add(cb.equal(root.get("id"), filter.roomId()));
            }

            if (filter.nameContains() != null && !filter.nameContains().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("name")),
                        "%" + filter.nameContains().toLowerCase() + "%"
                ));
            }

            if (filter.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("price"),
                        filter.minPrice()
                ));
            }

            if (filter.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("price"),
                        filter.maxPrice()
                ));
            }

            if (filter.minGuestCount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("maxCount"),
                        filter.minGuestCount().byteValue()
                ));
            }

            if (filter.startDate() != null && filter.endDate() != null) {
                Join<Room, Booking> bookingJoin = root.join("bookings", JoinType.LEFT);
                bookingJoin.on(
                        cb.and(
                                cb.lessThan(bookingJoin.get("startDate"), filter.endDate()),
                                cb.greaterThan(bookingJoin.get("endDate"), filter.startDate())
                        )
                );

                predicates.add(cb.isNull(bookingJoin.get("id")));
                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}