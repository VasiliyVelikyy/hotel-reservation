package ru.moskalev.hotel_reservation.service.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.moskalev.hotel_reservation.dto.kafka.OutboxEvent;
import ru.moskalev.hotel_reservation.dto.kafka.RoomBookedEvent;
import ru.moskalev.hotel_reservation.dto.kafka.UserRegisteredEvent;
import ru.moskalev.hotel_reservation.exception.TransactionalOutboxException;
import ru.moskalev.hotel_reservation.repo.OutboxEventRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventOutboxServiceTest {

    @Mock
    private OutboxEventRepository outboxRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private EventOutboxService eventOutboxService;

    private UserRegisteredEvent validUserEvent;
    private RoomBookedEvent validRoomEvent;
    private OutboxEvent sampleOutboxEvent;

    @BeforeEach
    void setUp() {
        validUserEvent = new UserRegisteredEvent(1L);
        validRoomEvent = new RoomBookedEvent(2L, LocalDate.now(), LocalDate.now().plusDays(5));

        sampleOutboxEvent = OutboxEvent.builder()
                .id(1L)
                .eventType("USER_REGISTERED")
                .payload("{\"userId\":1}")
                .createdAt(LocalDateTime.now())
                .processed(false)
                .build();
    }

    @Test
    @DisplayName("saveToOutbox: Успешное сохранение события в outbox")
    void saveToOutbox_Success() throws JsonProcessingException {
        String expectedJson = "{\"userId\":1}";
        when(objectMapper.writeValueAsString(validUserEvent)).thenReturn(expectedJson);
        when(outboxRepository.save(any(OutboxEvent.class))).thenReturn(sampleOutboxEvent);

        eventOutboxService.saveToOutbox("USER_REGISTERED", validUserEvent);

        verify(objectMapper, times(1)).writeValueAsString(validUserEvent);

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxRepository, times(1)).save(captor.capture());

        OutboxEvent savedEvent = captor.getValue();
        assertThat(savedEvent.getEventType()).isEqualTo("USER_REGISTERED");
        assertThat(savedEvent.getPayload()).isEqualTo(expectedJson);
        assertThat(savedEvent.isProcessed()).isFalse();
        assertThat(savedEvent.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("saveToOutbox: Выбрасывает TransactionalOutboxException при ошибке сериализации")
    void saveToOutbox_JsonProcessingException() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(validUserEvent))
                .thenThrow(new JsonProcessingException("Mocked serialization error") {
                });

        assertThatThrownBy(() -> eventOutboxService.saveToOutbox("USER_REGISTERED", validUserEvent))
                .isInstanceOf(TransactionalOutboxException.class)
                .hasMessageContaining("Failed to serialize outbox event");

        verify(outboxRepository, never()).save(any());
    }

    @Test
    @DisplayName("findTopUnprocessedEvents: Успешный возврат списка необработанных событий")
    void findTopUnprocessedEvents_Success() {
        int batchSize = 10;
        List<OutboxEvent> expectedEvents = List.of(sampleOutboxEvent);
        when(outboxRepository.findTopUnprocessedEvents(batchSize)).thenReturn(expectedEvents);

        List<OutboxEvent> result = eventOutboxService.findTopUnprocessedEvents(batchSize);

        assertThat(result).isNotNull().hasSize(1).containsExactly(sampleOutboxEvent);
        verify(outboxRepository, times(1)).findTopUnprocessedEvents(batchSize);
    }

    @Test
    @DisplayName("deserializePayload: Успешная десериализация JSON в объект события")
    void deserializePayload_Success() throws JsonProcessingException {
        String jsonPayload = "{\"userId\":1, \"checkInDate\":\"2023-10-01\", \"checkOutDate\":\"2023-10-05\"}";
        when(objectMapper.readValue(eq(jsonPayload), eq(RoomBookedEvent.class))).thenReturn(validRoomEvent);

        RoomBookedEvent result = eventOutboxService.deserializePayload(jsonPayload, RoomBookedEvent.class);

        assertThat(result).isNotNull().isEqualTo(validRoomEvent);
        verify(objectMapper, times(1)).readValue(jsonPayload, RoomBookedEvent.class);
    }

    @Test
    @DisplayName("deserializePayload: Выбрасывает RuntimeException при ошибке десериализации")
    void deserializePayload_JsonProcessingException() throws JsonProcessingException {
        String invalidJson = "{invalid json}";
        when(objectMapper.readValue(eq(invalidJson), eq(UserRegisteredEvent.class)))
                .thenThrow(new JsonProcessingException("Mocked deserialization error") {
                });

        assertThatThrownBy(() -> eventOutboxService.deserializePayload(invalidJson, UserRegisteredEvent.class))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to deserialize payload to UserRegisteredEvent");

        verify(objectMapper, times(1)).readValue(invalidJson, UserRegisteredEvent.class);
    }

    @Test
    @DisplayName("delete: Успешное удаление события из репозитория")
    void delete_Success() {
        eventOutboxService.delete(sampleOutboxEvent);

        verify(outboxRepository, times(1)).delete(sampleOutboxEvent);
    }
}