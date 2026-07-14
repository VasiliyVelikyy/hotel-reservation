package ru.moskalev.hotel_reservation.service.outbox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.moskalev.hotel_reservation.dto.kafka.OutboxEvent;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxProcessorTests {

    @Mock
    private EventOutboxService eventOutboxService;
    @Mock
    private OutboxEventHandler eventHandler;

    private OutboxProcessor outboxProcessor;

    @BeforeEach
    void setUp() {
        outboxProcessor = new OutboxProcessor(eventHandler, eventOutboxService);
        ReflectionTestUtils.setField(outboxProcessor, "batchSize", 100);
    }

    @Test
    void processOutboxEvents_EmptyList_DoesNothing() {
        when(eventOutboxService.findTopUnprocessedEvents(100)).thenReturn(List.of());

        outboxProcessor.processOutboxEvents();

        verify(eventHandler, never()).processSingleEvent(any());
    }

    @Test
    void processOutboxEvents_ExceptionInOneEvent_DoesNotStopLoop() {
        OutboxEvent event1 = OutboxEvent.builder().id(1L).eventType("USER_REGISTERED").build();
        OutboxEvent event2 = OutboxEvent.builder().id(2L).eventType("USER_REGISTERED").build();

        when(eventOutboxService.findTopUnprocessedEvents(100)).thenReturn(List.of(event1, event2));

        doThrow(new RuntimeException("Kafka down")).when(eventHandler).processSingleEvent(event1);
        doNothing().when(eventHandler).processSingleEvent(event2);

        outboxProcessor.processOutboxEvents();

        verify(eventHandler, times(1)).processSingleEvent(event1);
        verify(eventHandler, times(1)).processSingleEvent(event2);
    }
}