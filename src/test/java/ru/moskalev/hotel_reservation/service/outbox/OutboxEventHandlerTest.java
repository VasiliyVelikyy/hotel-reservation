package ru.moskalev.hotel_reservation.service.outbox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.moskalev.hotel_reservation.dto.kafka.OutboxEvent;
import ru.moskalev.hotel_reservation.dto.kafka.RoomBookedEvent;
import ru.moskalev.hotel_reservation.dto.kafka.UserRegisteredEvent;
import ru.moskalev.hotel_reservation.integration.kafka.KafkaStatsPublisher;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static ru.moskalev.hotel_reservation.enumeration.KafkaEvent.ROOM_BOOKED;
import static ru.moskalev.hotel_reservation.enumeration.KafkaEvent.USER_REGISTERED;

@ExtendWith(MockitoExtension.class)
class OutboxEventHandlerTest {

    @Mock
    private EventOutboxService eventOutboxService;
    @Mock
    private KafkaStatsPublisher publisher;

    @InjectMocks
    private OutboxEventHandler eventHandler;

    @Test
    void processSingleEvent_UserRegistered_DeserializesAndPublishes() {
        OutboxEvent event = OutboxEvent.builder()
                .id(1L).eventType(USER_REGISTERED.name()).payload("{\"userId\":42}").build();
        UserRegisteredEvent userEvent = new UserRegisteredEvent(42L);

        when(eventOutboxService.deserializePayload("{\"userId\":42}", UserRegisteredEvent.class))
                .thenReturn(userEvent);
        doNothing().when(publisher).publishUserEvent(userEvent);
        doNothing().when(eventOutboxService).delete(event);

        eventHandler.processSingleEvent(event);

        verify(publisher).publishUserEvent(userEvent);
        verify(eventOutboxService).delete(event);
    }

    @Test
    void processSingleEvent_RoomBooked_DeserializesAndPublishes() {
        OutboxEvent event = OutboxEvent.builder()
                .id(2L).eventType(ROOM_BOOKED.name()).payload("{\"userId\":1}").build();
        RoomBookedEvent bookedEvent = new RoomBookedEvent(1L, LocalDate.now(), LocalDate.now().plusDays(1));

        when(eventOutboxService.deserializePayload(any(), eq(RoomBookedEvent.class)))
                .thenReturn(bookedEvent);
        doNothing().when(publisher).publishBookingEvent(bookedEvent);
        doNothing().when(eventOutboxService).delete(event);

        eventHandler.processSingleEvent(event);

        verify(publisher).publishBookingEvent(bookedEvent);
        verify(eventOutboxService).delete(event);
    }

    @Test
    void processSingleEvent_UnknownType_ThrowsException() {
        OutboxEvent event = OutboxEvent.builder()
                .id(3L).eventType("UNKNOWN_EVENT").payload("{}").build();

        assertThrows(IllegalArgumentException.class,
                () -> eventHandler.processSingleEvent(event));

        verify(eventOutboxService, never()).delete(event);
    }
}