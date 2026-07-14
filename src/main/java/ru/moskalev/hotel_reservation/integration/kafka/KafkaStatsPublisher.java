package ru.moskalev.hotel_reservation.integration.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import ru.moskalev.hotel_reservation.dto.kafka.BaseUserEvent;
import ru.moskalev.hotel_reservation.dto.kafka.RoomBookedEvent;
import ru.moskalev.hotel_reservation.dto.kafka.UserRegisteredEvent;
import ru.moskalev.hotel_reservation.exception.KafkaStatsException;
import ru.moskalev.hotel_reservation.service.outbox.EventOutboxService;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaStatsPublisher {

    private final KafkaTemplate<@NonNull Object, @NonNull Object> kafkaTemplate;
    private final EventOutboxService eventOutboxService;

    @Value("${spring.kafka.topics.user}")
    private String userTopic;

    @Value("${spring.kafka.topics.booking}")
    private String bookingTopic;

    public void publishUserEventAfterCommit(UserRegisteredEvent userEvent) {
        sendAfterCommit(userTopic, userEvent);
    }

    public void publishUserEvent(UserRegisteredEvent userEvent) {
        doSend(userTopic, userEvent);
    }

    public void publishBookingEventAfterCommit(RoomBookedEvent roomBookedEvent) {
        sendAfterCommit(bookingTopic, roomBookedEvent);
    }

    public void publishBookingEvent(RoomBookedEvent roomBookedEvent) {
        doSend(bookingTopic, roomBookedEvent);
    }

    private void sendAfterCommit(String topic, BaseUserEvent event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    doSend(topic, event);
                }
            });
        } else {
            doSend(topic, event);
        }
    }

    private void doSend(String topic, BaseUserEvent event) {
        try {
            kafkaTemplate.send(topic, String.valueOf(event.userId()), event);
            log.info("Event sent to topic {}: {}", topic, event.getClass().getSimpleName());
        } catch (Exception e) {
            log.error("Failed sent to topic {}: {}, {}", topic, event.getClass().getSimpleName(), e.getMessage());
            eventOutboxService.saveToOutbox(event.getEventType(), event);
            throw new KafkaStatsException("Kafka send failed", e);
        }
    }
}
