package ru.moskalev.hotel_reservation.service.outbox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.moskalev.hotel_reservation.dto.kafka.OutboxEvent;
import ru.moskalev.hotel_reservation.dto.kafka.RoomBookedEvent;
import ru.moskalev.hotel_reservation.dto.kafka.UserRegisteredEvent;
import ru.moskalev.hotel_reservation.enumeration.KafkaEvent;
import ru.moskalev.hotel_reservation.integration.kafka.KafkaStatsPublisher;
import ru.moskalev.hotel_reservation.repo.OutboxEventRepository;
import ru.moskalev.hotel_reservation.service.BaseIntegrationTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

class OutboxIntegrationTests extends BaseIntegrationTest {

    @Autowired
    private EventOutboxService eventOutboxService;
    @Autowired
    private OutboxEventHandler eventHandler;
    @Autowired
    private OutboxEventRepository outboxRepository;

    @MockitoBean
    private KafkaStatsPublisher publisher;

    @BeforeEach
    void setUp() {
        outboxRepository.deleteAll();
    }

    @Test
    void processSingleEvent_OnKafkaFailure_RollbackTransactionAndKeepEvent() {
        UserRegisteredEvent payload = new UserRegisteredEvent(99L);
        eventOutboxService.saveToOutbox(KafkaEvent.USER_REGISTERED.name(), payload);

        List<OutboxEvent> events = outboxRepository.findTopUnprocessedEvents(10);
        assertEquals(1, events.size());
        OutboxEvent event = events.getFirst();

        doThrow(new RuntimeException("Kafka is down!"))
                .when(publisher).publishUserEvent(any(UserRegisteredEvent.class));

        assertThrows(RuntimeException.class, () -> eventHandler.processSingleEvent(event));

        List<OutboxEvent> remainingEvents = outboxRepository.findTopUnprocessedEvents(10);
        assertEquals(1, remainingEvents.size());
        assertEquals(event.getId(), remainingEvents.getFirst().getId());
    }

    @Test
    void processSingleEvent_OnSuccess_DeletesEvent() {
        RoomBookedEvent payload = new RoomBookedEvent(1L, LocalDate.now(), LocalDate.now().plusDays(1));
        eventOutboxService.saveToOutbox(KafkaEvent.ROOM_BOOKED.name(), payload);

        OutboxEvent event = outboxRepository.findTopUnprocessedEvents(10).getFirst();

        doNothing().when(publisher).publishBookingEvent(any(RoomBookedEvent.class));

        eventHandler.processSingleEvent(event);

        List<OutboxEvent> remainingEvents = outboxRepository.findTopUnprocessedEvents(10);
        assertTrue(remainingEvents.isEmpty(), "Event should be deleted after successful processing");
    }
}