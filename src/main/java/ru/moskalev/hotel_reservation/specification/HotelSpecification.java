package ru.moskalev.hotel_reservation.specification;

import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.domain.Specification;
import ru.moskalev.hotel_reservation.domain.Hotel;
import ru.moskalev.hotel_reservation.dto.hotel.HotelFilter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HotelSpecification {

    public static Specification<@NonNull Hotel> withFilter(HotelFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            if (filter.city() != null && !filter.city().isBlank()) {
                predicates.add(cb.equal(root.get("city"), filter.city()));
            }

            if (filter.nameContains() != null && !filter.nameContains().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")),
                        "%" + filter.nameContains().toLowerCase() + "%"));
            }

            if (filter.minDistance() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("distance"), filter.minDistance()));
            }

            if (filter.maxDistance() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("distance"), filter.maxDistance()));
            }

            if (filter.minRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("rating"),
                        filter.minRating()
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
