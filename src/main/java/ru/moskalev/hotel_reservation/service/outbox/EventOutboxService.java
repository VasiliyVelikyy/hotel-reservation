package ru.moskalev.hotel_reservation.service.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.moskalev.hotel_reservation.dto.kafka.BaseUserEvent;
import ru.moskalev.hotel_reservation.dto.kafka.OutboxEvent;
import ru.moskalev.hotel_reservation.exception.TransactionalOutboxException;
import ru.moskalev.hotel_reservation.repo.OutboxEventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventOutboxService {
    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public void saveToOutbox(String eventType, BaseUserEvent eventPayload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(eventPayload);
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .eventType(eventType)
                    .payload(jsonPayload)
                    .createdAt(LocalDateTime.now())
                    .processed(false)
                    .build();

            outboxRepository.save(outboxEvent);
            log.info("Event saved to outbox table: {}", eventType);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize outbox event: {}", eventType, e);
            throw new TransactionalOutboxException("Failed to serialize outbox event", e);
        }
    }

    public List<OutboxEvent> findTopUnprocessedEvents(int batchSize) {
        return outboxRepository.findTopUnprocessedEvents(batchSize);
    }

    public <T> T deserializePayload(String payload, Class<T> targetType) {
        try {
            return objectMapper.readValue(payload, targetType);
        } catch (JsonProcessingException e) {
            String errorMsg = String.format("Failed to deserialize payload to %s", targetType.getSimpleName());
            log.error("{} Payload: {}", errorMsg, payload, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    public void delete(OutboxEvent event) {
        outboxRepository.delete(event);
    }
}
