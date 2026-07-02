package ru.moskalev.hotel_reservation.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.moskalev.hotel_reservation.dto.room.RoomCreateInput;
import ru.moskalev.hotel_reservation.dto.room.RoomResponse;
import ru.moskalev.hotel_reservation.dto.room.RoomUpdateInput;
import ru.moskalev.hotel_reservation.exception.EntityNotFoundException;
import ru.moskalev.hotel_reservation.service.HotelService;
import ru.moskalev.hotel_reservation.service.RoomService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.moskalev.hotel_reservation.Constants.*;

@WebMvcTest(RoomController.class)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private RoomService roomService;

    @MockitoBean
    private HotelService hotelService;

    private static final String BASE_URL = V1 + ROOM;
    private static final Long HOTEL_ID_VALUE = 1L;
    private static final Long ROOM_ID_VALUE = 1L;

    @Test
    @DisplayName("POST /{hotelId} — 201 — номер успешно создан")
    void create_success() throws Exception {
        RoomCreateInput input = new RoomCreateInput(
                "Люкс",
                "Просторный номер",
                (short) 101,
                new BigDecimal("5000.00"),
                (byte) 2,
                1700000000L,
                1700100000L
        );

        RoomResponse expected = new RoomResponse(
                1L,
                "Люкс",
                "Просторный номер",
                (short) 101,
                new BigDecimal("5000.00"),
                (byte) 2,
                1700000000L,
                1700100000L,
                HOTEL_ID_VALUE
        );

        when(roomService.create(eq(HOTEL_ID_VALUE), any(RoomCreateInput.class))).thenReturn(expected);

        mockMvc.perform(post(BASE_URL + PATH + HOTEL_ID_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Люкс"))
                .andExpect(jsonPath("$.description").value("Просторный номер"))
                .andExpect(jsonPath("$.number").value(101))
                .andExpect(jsonPath("$.price").value(5000.00))
                .andExpect(jsonPath("$.maxCount").value(2))
                .andExpect(jsonPath("$.freeStartDate").value(1700000000L))
                .andExpect(jsonPath("$.freeEndDate").value(1700100000L))
                .andExpect(jsonPath("$.hotelId").value(HOTEL_ID_VALUE));
    }

    @Test
    @DisplayName("POST /{hotelId} — 400 — невалидные входные данные (пустой name)")
    void create_badRequest_emptyName() throws Exception {
        RoomCreateInput invalidInput = new RoomCreateInput(
                "",
                "Description",
                (short) 101,
                new BigDecimal("5000.00"),
                (byte) 2,
                1700000000L,
                1700100000L
        );

        mockMvc.perform(post(BASE_URL + PATH + HOTEL_ID_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInput)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("POST /{hotelId} — 400 — отрицательная цена")
    void create_badRequest_negativePrice() throws Exception {
        RoomCreateInput invalidInput = new RoomCreateInput(
                "Люкс",
                "Description",
                (short) 101,
                new BigDecimal("-100.00"),
                (byte) 2,
                1700000000L,
                1700100000L
        );

        mockMvc.perform(post(BASE_URL + PATH + HOTEL_ID_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInput)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    @DisplayName("POST /{hotelId} — 404 — отель не найден")
    void create_hotelNotFound() throws Exception {
        RoomCreateInput input = new RoomCreateInput(
                "Люкс", "Description", (short) 101,
                new BigDecimal("5000.00"), (byte) 2,
                1700000000L, 1700100000L
        );

        doThrow(new EntityNotFoundException("Hotel with id 999 not found"))
                .when(roomService).create(eq(999L), any(RoomCreateInput.class));

        mockMvc.perform(post(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.description").value("Hotel with id 999 not found"));
    }

    @Test
    @DisplayName("GET /{roomId} — 200 — номер найден")
    void getById_success() throws Exception {
        RoomResponse expected = new RoomResponse(
                ROOM_ID_VALUE,
                "Люкс",
                "Просторный номер",
                (short) 101,
                new BigDecimal("5000.00"),
                (byte) 2,
                1700000000L,
                1700100000L,
                HOTEL_ID_VALUE
        );

        when(roomService.getById(ROOM_ID_VALUE)).thenReturn(expected);

        mockMvc.perform(get(BASE_URL + PATH + ROOM_ID_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ROOM_ID_VALUE))
                .andExpect(jsonPath("$.name").value("Люкс"))
                .andExpect(jsonPath("$.number").value(101))
                .andExpect(jsonPath("$.hotelId").value(HOTEL_ID_VALUE));
    }

    @Test
    @DisplayName("GET /{roomId} — 404 — номер не найден")
    void getById_notFound() throws Exception {
        when(roomService.getById(999L))
                .thenThrow(new EntityNotFoundException("Room with id 999 not found"));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.description").value("Room with id 999 not found"));
    }

    @Test
    @DisplayName("GET /{hotelId} — 200 — список номеров успешно получен")
    void getAllByHotelId_success() throws Exception {
        List<RoomResponse> content = List.of(
                new RoomResponse(1L, "Люкс", "Desc 1", (short) 101,
                        new BigDecimal("5000.00"), (byte) 2,
                        1700000000L, 1700100000L, HOTEL_ID_VALUE),
                new RoomResponse(2L, "Стандарт", "Desc 2", (short) 102,
                        new BigDecimal("3000.00"), (byte) 1,
                        1700000000L, 1700100000L, HOTEL_ID_VALUE)
        );
        Page<RoomResponse> page = new PageImpl<>(content);

        when(roomService.getAllByHotelId(eq(HOTEL_ID_VALUE), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get(BASE_URL + HOTEL + PATH + HOTEL_ID_VALUE)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Люкс"))
                .andExpect(jsonPath("$.content[0].number").value(101))
                .andExpect(jsonPath("$.content[1].name").value("Стандарт"))
                .andExpect(jsonPath("$.content[1].number").value(102));
    }

    @Test
    @DisplayName("GET /{hotelId} — 200 — список пустой")
    void getAllByHotelId_empty() throws Exception {
        Page<RoomResponse> emptyPage = Page.empty();
        when(roomService.getAllByHotelId(eq(HOTEL_ID_VALUE), any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get(BASE_URL + HOTEL + PATH + HOTEL_ID_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @DisplayName("GET /{hotelId} — 404 — отель не найден")
    void getAllByHotelId_hotelNotFound() throws Exception {
        when(roomService.getAllByHotelId(eq(999L), any(Pageable.class)))
                .thenThrow(new EntityNotFoundException("Hotel with id 999 not found"));

        mockMvc.perform(get(BASE_URL + HOTEL + "/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    @Test
    @DisplayName("GET /{hotelId} — 400 — невалидное направление сортировки")
    void getAllByHotelId_badSortDirection() throws Exception {
        mockMvc.perform(get(BASE_URL + HOTEL + PATH + HOTEL_ID_VALUE)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id")
                        .param("direction", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    @DisplayName("PUT /{roomId} — 200 — номер успешно обновлён")
    void update_success() throws Exception {
        RoomUpdateInput input = new RoomUpdateInput(
                "Новый Люкс",
                "Новое описание",
                (short) 202,
                new BigDecimal("6000.00"),
                (byte) 3,
                1700200000000L,
                1700300000000L
        );

        RoomResponse expected = new RoomResponse(
                ROOM_ID_VALUE,
                "Новый Люкс",
                "Новое описание",
                (short) 202,
                new BigDecimal("6000.00"),
                (byte) 3,
                1700200000000L,
                1700300000000L,
                HOTEL_ID_VALUE
        );

        when(roomService.update(eq(ROOM_ID_VALUE), any(RoomUpdateInput.class))).thenReturn(expected);

        mockMvc.perform(put(BASE_URL + PATH + ROOM_ID_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ROOM_ID_VALUE))
                .andExpect(jsonPath("$.name").value("Новый Люкс"))
                .andExpect(jsonPath("$.description").value("Новое описание"))
                .andExpect(jsonPath("$.number").value(202))
                .andExpect(jsonPath("$.price").value(6000.00))
                .andExpect(jsonPath("$.maxCount").value(3));
    }

    @Test
    @DisplayName("PUT /{roomId} — 200 — частичное обновление (только name)")
    void update_partialOnlyName() throws Exception {
        RoomUpdateInput input = new RoomUpdateInput(
                "Обновлённый Люкс",
                null, null, null, null, null, null
        );

        RoomResponse expected = new RoomResponse(
                ROOM_ID_VALUE,
                "Обновлённый Люкс",
                "Старое описание",
                (short) 101,
                new BigDecimal("5000.00"),
                (byte) 2,
                1700000000L,
                1700100000L,
                HOTEL_ID_VALUE
        );

        when(roomService.update(eq(ROOM_ID_VALUE), any(RoomUpdateInput.class))).thenReturn(expected);

        mockMvc.perform(put(BASE_URL + PATH + ROOM_ID_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Обновлённый Люкс"))
                .andExpect(jsonPath("$.description").value("Старое описание"))
                .andExpect(jsonPath("$.number").value(101));
    }

    @Test
    @DisplayName("PUT /{roomId} — 404 — номер для обновления не найден")
    void update_notFound() throws Exception {
        RoomUpdateInput input = new RoomUpdateInput(
                "X", null, null, null, null, null, null
        );

        when(roomService.update(eq(999L), any(RoomUpdateInput.class)))
                .thenThrow(new EntityNotFoundException("Room with id 999 not found"));

        mockMvc.perform(put(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.description").value("Room with id 999 not found"));
    }

    @Test
    @DisplayName("PUT /{roomId} — 400 — name слишком длинный")
    void update_badRequest_nameTooLong() throws Exception {
        RoomUpdateInput input = new RoomUpdateInput(
                "A".repeat(101),
                null, null, null, null, null, null
        );

        mockMvc.perform(put(BASE_URL + PATH + ROOM_ID_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    @DisplayName("DELETE /{roomId} — 204 — номер успешно удалён")
    void delete_success() throws Exception {
        doNothing().when(roomService).delete(ROOM_ID_VALUE);

        mockMvc.perform(delete(BASE_URL + PATH + ROOM_ID_VALUE))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /{roomId} — 404 — номер для удаления не найден")
    void delete_notFound() throws Exception {
        doThrow(new EntityNotFoundException("Room with id 999 not found"))
                .when(roomService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.description").value("Room with id 999 not found"));
    }
}