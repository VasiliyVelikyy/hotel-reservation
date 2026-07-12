package ru.moskalev.hotel_reservation.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ru.moskalev.hotel_reservation.domain.StatEventDocument;

import java.util.stream.Stream;

@Repository
public interface StatEventRepository extends MongoRepository<StatEventDocument, String> {

    Stream<StatEventDocument> findAllBy();
}