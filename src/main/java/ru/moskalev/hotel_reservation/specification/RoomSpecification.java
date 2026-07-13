package ru.moskalev.hotel_reservation.specification;

import jakarta.persistence.criteria.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.domain.Specification;
import ru.moskalev.hotel_reservation.domain.Booking;
import ru.moskalev.hotel_reservation.domain.Room;
import ru.moskalev.hotel_reservation.dto.room.RoomFilter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RoomSpecification {

    public static Specification<@NonNull Room> withFilter(RoomFilter filter) {
        return (root, query, cb) -> {
            if (filter == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            addIdPredicate(filter, root, cb, predicates);
            addNamePredicate(filter, root, cb, predicates);
            addPricePredicates(filter, root, cb, predicates);
            addGuestCountPredicate(filter, root, cb, predicates);
            addDateAvailabilityPredicate(filter, root, query, cb, predicates);

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private static void addIdPredicate(RoomFilter filter, Root<Room> root,
                                       CriteriaBuilder cb, List<Predicate> predicates) {
        if (filter.roomId() != null) {
            predicates.add(cb.equal(root.get("id"), filter.roomId()));
        }
    }

    private static void addNamePredicate(RoomFilter filter, Root<Room> root,
                                         CriteriaBuilder cb, List<Predicate> predicates) {
        if (isNotBlank(filter.nameContains())) {
            predicates.add(cb.like(
                    cb.lower(root.get("name")),
                    "%" + filter.nameContains().toLowerCase() + "%"
            ));
        }
    }

    private static void addPricePredicates(RoomFilter filter, Root<Room> root,
                                           CriteriaBuilder cb, List<Predicate> predicates) {
        if (filter.minPrice() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.minPrice()));
        }
        if (filter.maxPrice() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.maxPrice()));
        }
    }

    private static void addGuestCountPredicate(RoomFilter filter, Root<Room> root,
                                               CriteriaBuilder cb, List<Predicate> predicates) {
        if (filter.minGuestCount() != null) {
            predicates.add(cb.greaterThanOrEqualTo(
                    root.get("maxCount"),
                    filter.minGuestCount().byteValue()
            ));
        }
    }

    private static void addDateAvailabilityPredicate(RoomFilter filter, Root<Room> root,
                                                     CriteriaQuery<?> query,
                                                     CriteriaBuilder cb,
                                                     List<Predicate> predicates) {
        if (filter.startDate() == null || filter.endDate() == null) {
            return;
        }

        Join<Room, Booking> bookingJoin = root.join("bookings", JoinType.LEFT);
        bookingJoin.on(cb.and(
                cb.lessThan(bookingJoin.get("startDate"), filter.endDate()),
                cb.greaterThan(bookingJoin.get("endDate"), filter.startDate())
        ));

        predicates.add(cb.isNull(bookingJoin.get("id")));
        query.distinct(true);
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}