package ru.moskalev.hotel_reservation.service.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.moskalev.hotel_reservation.dto.kafka.OutboxEvent;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxProcessor {
    private final OutboxEventHandler eventHandler;
    private final EventOutboxService eventOutboxService;

    @Value("${spring.outbox.polling.batch-size}")
    private int batchSize;


    @Scheduled(fixedDelayString = "${spring.outbox.polling.delay.ms:5000}")
    public void processOutboxEvents() {
        List<OutboxEvent> events = eventOutboxService.findTopUnprocessedEvents(batchSize);

        if (events.isEmpty()) {
            return;
        }
        log.info("Found {} unprocessed outbox events. Starting processing...", events.size());

        for (OutboxEvent event : events) {
            try {
                eventHandler.processSingleEvent(event);
            } catch (Exception e) {
                log.error("Critical error processing outbox event ID: {}. Will retry on next cycle.", event.getId(), e);

            }
        }
    }
}