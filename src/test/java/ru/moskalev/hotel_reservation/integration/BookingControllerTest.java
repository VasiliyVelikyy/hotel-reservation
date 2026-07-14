package ru.moskalev.hotel_reservation.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.moskalev.hotel_reservation.dto.booking.BookingCreateRequest;
import ru.moskalev.hotel_reservation.dto.booking.BookingResponse;
import ru.moskalev.hotel_reservation.exception.EntityNotFoundException;
import ru.moskalev.hotel_reservation.integration.rest.BookingController;
import ru.moskalev.hotel_reservation.service.BookingService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.moskalev.hotel_reservation.domain.Constants.BOOKING;
import static ru.moskalev.hotel_reservation.domain.Constants.V1;

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @MockitoBean
    private BookingService bookingService;

    private static final String BASE_URL = V1 + BOOKING;
    private static final Long BOOKING_ID_VALUE = 1L;

    @Test
    @DisplayName("POST /booking — 201 — бронирование успешно создано")
    void create_success() throws Exception {
        BookingCreateRequest input = new BookingCreateRequest(
                5L,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 15),
                2
        );

        BookingResponse expected = new BookingResponse(
                1L,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 15),
                2,
                5L,
                100L
        );

        when(bookingService.create(any(BookingCreateRequest.class))).thenReturn(expected);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.startDate").value("2026-07-10"))
                .andExpect(jsonPath("$.endDate").value("2026-07-15"))
                .andExpect(jsonPath("$.guestCount").value(2))
                .andExpect(jsonPath("$.roomId").value(5L))
                .andExpect(jsonPath("$.userId").value(100L));

        verify(bookingService).create(any(BookingCreateRequest.class));
    }

    @Test
    @DisplayName("POST /booking — 400 — roomId равен null")
    void create_badRequest_nullRoomId() throws Exception {
        BookingCreateRequest invalidInput = new BookingCreateRequest(
                null,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 15),
                2
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInput)))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).create(any(BookingCreateRequest.class));
    }

    @Test
    @DisplayName("POST /booking — 400 — дата заезда позже даты выезда")
    void create_badRequest_invalidDates() throws Exception {
        BookingCreateRequest invalidInput = new BookingCreateRequest(
                5L,
                LocalDate.of(2026, 7, 20), // заезд ПОСЛЕ выезда
                LocalDate.of(2026, 7, 15),
                2
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInput)))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).create(any(BookingCreateRequest.class));
    }

    @Test
    @DisplayName("POST /booking — 400 — количество гостей меньше 1")
    void create_badRequest_guestCountTooLow() throws Exception {
        BookingCreateRequest invalidInput = new BookingCreateRequest(
                5L,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 15),
                0
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInput)))
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).create(any(BookingCreateRequest.class));
    }

    @Test
    @DisplayName("GET /booking/{bookingId} — 200 — бронирование найдено")
    void getById_success() throws Exception {
        BookingResponse expected = new BookingResponse(
                BOOKING_ID_VALUE,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 15),
                2,
                5L,
                100L
        );

        when(bookingService.getById(BOOKING_ID_VALUE)).thenReturn(expected);

        mockMvc.perform(get(BASE_URL + "/{bookingId}", BOOKING_ID_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(BOOKING_ID_VALUE))
                .andExpect(jsonPath("$.startDate").value("2026-07-10"))
                .andExpect(jsonPath("$.endDate").value("2026-07-15"))
                .andExpect(jsonPath("$.guestCount").value(2))
                .andExpect(jsonPath("$.roomId").value(5L))
                .andExpect(jsonPath("$.userId").value(100L));

        verify(bookingService).getById(BOOKING_ID_VALUE);
    }

    @Test
    @DisplayName("GET /booking/{bookingId} — 404 — бронирование не найдено")
    void getById_notFound() throws Exception {
        when(bookingService.getById(BOOKING_ID_VALUE))
                .thenThrow(new EntityNotFoundException("Booking not found"));

        mockMvc.perform(get(BASE_URL + "/{bookingId}", BOOKING_ID_VALUE))
                .andExpect(status().isNotFound());

        verify(bookingService).getById(BOOKING_ID_VALUE);
    }

    @Test
    @DisplayName("GET /booking/my — 200 — список бронирований текущего пользователя")
    void getMyBookings_success() throws Exception {
        List<BookingResponse> expected = List.of(
                new BookingResponse(1L, LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 15), 2, 5L, 100L),
                new BookingResponse(2L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 5), 1, 3L, 100L)
        );

        when(bookingService.getMyBookings()).thenReturn(expected);

        mockMvc.perform(get(BASE_URL + "/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].roomId").value(5L))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].roomId").value(3L));

        verify(bookingService).getMyBookings();
    }

    @Test
    @DisplayName("GET /booking/my — 200 — пустой список бронирований")
    void getMyBookings_empty() throws Exception {
        when(bookingService.getMyBookings()).thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_URL + "/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(bookingService).getMyBookings();
    }

    @Test
    @DisplayName("GET /booking — 200 — список всех бронирований с пагинацией по умолчанию")
    void getAllBookings_success() throws Exception {
        BookingResponse booking1 = new BookingResponse(1L, LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 15), 2, 5L, 100L);
        BookingResponse booking2 = new BookingResponse(2L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 5), 1, 3L, 200L);

        var page = new PageImpl<>(
                List.of(booking1, booking2),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id")),
                2
        );

        when(bookingService.getAllBookings(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10));

        verify(bookingService).getAllBookings(any(Pageable.class));
    }

    @Test
    @DisplayName("GET /booking — 200 — пагинация с кастомными параметрами")
    void getAllBookings_customPagination() throws Exception {
        Page<@NonNull BookingResponse> page = new PageImpl<>(
                Collections.emptyList(),
                PageRequest.of(1, 5, Sort.by(Sort.Direction.DESC, "startDate")),
                0
        );

        when(bookingService.getAllBookings(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get(BASE_URL)
                        .param("page", "1")
                        .param("size", "5")
                        .param("sortBy", "startDate")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(5));

        verify(bookingService).getAllBookings(argThat(p ->
                p.getPageNumber() == 1 &&
                        p.getPageSize() == 5 &&
                        p.getSort().isSorted()
        ));
    }

    @Test
    @DisplayName("DELETE /booking/{bookingId} — 204 — бронирование успешно отменено")
    void cancel_success() throws Exception {
        doNothing().when(bookingService).cancel(BOOKING_ID_VALUE);

        mockMvc.perform(delete(BASE_URL + "/{bookingId}", BOOKING_ID_VALUE))
                .andExpect(status().isNoContent());

        verify(bookingService).cancel(BOOKING_ID_VALUE);
    }

    @Test
    @DisplayName("DELETE /booking/{bookingId} — 404 — бронирование для отмены не найдено")
    void cancel_notFound() throws Exception {
        doThrow(new EntityNotFoundException("Booking not found"))
                .when(bookingService).cancel(BOOKING_ID_VALUE);

        mockMvc.perform(delete(BASE_URL + "/{bookingId}", BOOKING_ID_VALUE))
                .andExpect(status().isNotFound());

        verify(bookingService).cancel(BOOKING_ID_VALUE);
    }
}