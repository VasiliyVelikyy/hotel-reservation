package ru.moskalev.hotel_reservation.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.moskalev.hotel_reservation.dto.grade.GradeResponse;
import ru.moskalev.hotel_reservation.dto.hotel.HotelCreateInput;
import ru.moskalev.hotel_reservation.dto.hotel.HotelFilter;
import ru.moskalev.hotel_reservation.dto.hotel.HotelResponse;
import ru.moskalev.hotel_reservation.dto.hotel.HotelUpdateInput;
import ru.moskalev.hotel_reservation.exception.EntityNotFoundException;
import ru.moskalev.hotel_reservation.integration.rest.HotelController;
import ru.moskalev.hotel_reservation.service.HotelService;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.moskalev.hotel_reservation.domain.Constants.*;

@WebMvcTest(HotelController.class)
class HotelControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private HotelService hotelService;

    private static final String BASE_URL = V1 + HOTEL;

    private static final Long HOTEL_ID_VALUE = 1L;

    @Test
    @DisplayName("POST /create — 201 — отель успешно создан")
    void create_success() throws Exception {
        HotelCreateInput input = new HotelCreateInput(
                "Hilton",
                "Luxury hotel",
                "Best Hotel",
                "Moscow",
                "Tverskaya 1",
                1500
        );

        HotelResponse expected = new HotelResponse(
                1L,
                "Hilton",
                "Luxury hotel",
                "Best Hotel",
                "Moscow",
                "Tverskaya 1",
                1500,
                0D,
                0D,
                0
        );

        when(hotelService.create(any(HotelCreateInput.class))).thenReturn(expected);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Hilton"))
                .andExpect(jsonPath("$.description").value("Luxury hotel"))
                .andExpect(jsonPath("$.title").value("Best Hotel"))
                .andExpect(jsonPath("$.city").value("Moscow"))
                .andExpect(jsonPath("$.address").value("Tverskaya 1"))
                .andExpect(jsonPath("$.distance").value(1500));
    }

    @Test
    @DisplayName("POST /create — 400 — невалидные входные данные (пустой name)")
    void create_badRequest_emptyName() throws Exception {
        HotelCreateInput invalidInput = new HotelCreateInput(
                "",
                "Description",
                "Title",
                "Moscow",
                "Address",
                1500
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInput)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("POST /create — 400 — name слишком длинный (>50 символов)")
    void create_badRequest_nameTooLong() throws Exception {
        HotelCreateInput invalidInput = new HotelCreateInput(
                "A".repeat(51),
                "Description",
                "Title",
                "Moscow",
                "Address",
                1500
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInput)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    @DisplayName("POST /create — 400 — description слишком длинный (>255 символов)")
    void create_badRequest_descriptionTooLong() throws Exception {
        HotelCreateInput invalidInput = new HotelCreateInput(
                "Name",
                "A".repeat(256),
                "Title",
                "Moscow",
                "Address",
                1500
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInput)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    @DisplayName("GET /{hotelId} — 200 — отель найден")
    void getById_success() throws Exception {
        HotelResponse expected = new HotelResponse(
                HOTEL_ID_VALUE,
                "Hilton",
                "Luxury hotel",
                "Best Hotel",
                "Moscow",
                "Tverskaya 1",
                1500, 0D, 0D, 0
        );
        when(hotelService.getById(HOTEL_ID_VALUE)).thenReturn(expected);

        mockMvc.perform(get(BASE_URL + PATH + HOTEL_ID_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(HOTEL_ID_VALUE))
                .andExpect(jsonPath("$.name").value("Hilton"))
                .andExpect(jsonPath("$.city").value("Moscow"))
                .andExpect(jsonPath("$.address").value("Tverskaya 1"))
                .andExpect(jsonPath("$.distance").value(1500));
    }

    @Test
    @DisplayName("GET /{hotelId} — 404 — отель не найден")
    void getById_notFound() throws Exception {
        when(hotelService.getById(999L))
                .thenThrow(new EntityNotFoundException("Hotel with id 999 not found"));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value("Not Found"))
                .andExpect(jsonPath("$.description").value("Hotel with id 999 not found"));
    }

    @Test
    @DisplayName("GET / — 200 — список отелей успешно получен")
    void getAll_success() throws Exception {
        List<HotelResponse> content = List.of(
                new HotelResponse(
                        1L, "Hilton", "Desc 1", "Title 1",
                        "Moscow", "Address 1", 1500, 0D, 0D, 0
                ),
                new HotelResponse(
                        2L, "Marriott", "Desc 2", "Title 2",
                        "SPB", "Address 2", 2000, 0D, 0D, 0
                )
        );
        var page = new PageImpl<>(content);

        when(hotelService.getAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Hilton"))
                .andExpect(jsonPath("$.content[0].city").value("Moscow"))
                .andExpect(jsonPath("$.content[0].distance").value(1500))
                .andExpect(jsonPath("$.content[1].name").value("Marriott"))
                .andExpect(jsonPath("$.content[1].city").value("SPB"));
    }

    @Test
    @DisplayName("GET / — 200 — список пустой")
    void getAll_empty() throws Exception {
        Page<@NonNull HotelResponse> emptyPage = Page.empty();
        when(hotelService.getAll(any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @DisplayName("PUT /{hotelId} — 200 — отель успешно обновлён")
    void update_success() throws Exception {
        HotelUpdateInput input = new HotelUpdateInput(
                "New Name",
                "New Description",
                "New Title",
                "New City",
                "New Address",
                1500
        );

        HotelResponse expected = new HotelResponse(
                HOTEL_ID_VALUE,
                "New Name",
                "New Description",
                "New Title",
                "New City",
                "New Address",
                1500,
                0D,
                0D,
                0
        );

        when(hotelService.update(eq(HOTEL_ID_VALUE), any(HotelUpdateInput.class)))
                .thenReturn(expected);

        mockMvc.perform(put(BASE_URL + PATH + HOTEL_ID_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(HOTEL_ID_VALUE))
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.description").value("New Description"))
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.city").value("New City"))
                .andExpect(jsonPath("$.address").value("New Address"))
                .andExpect(jsonPath("$.distance").value(1500));
    }

    @Test
    @DisplayName("PUT /{hotelId} — 200 — частичное обновление (только name)")
    void update_partialOnlyName() throws Exception {
        HotelUpdateInput input = new HotelUpdateInput(
                "Updated Name",
                null,
                null,
                null,
                null,
                null
        );

        HotelResponse expected = new HotelResponse(
                HOTEL_ID_VALUE,
                "Updated Name",
                "Old Description",
                "Old Title",
                "Old City",
                "Old Address",
                1000, 0D, 0D, 0
        );

        when(hotelService.update(eq(HOTEL_ID_VALUE), any(HotelUpdateInput.class)))
                .thenReturn(expected);

        mockMvc.perform(put(BASE_URL + PATH + HOTEL_ID_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(HOTEL_ID_VALUE))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.description").value("Old Description"))
                .andExpect(jsonPath("$.city").value("Old City"));
    }

    @Test
    @DisplayName("PUT /{hotelId} — 404 — отель для обновления не найден")
    void update_notFound() throws Exception {
        HotelUpdateInput input = new HotelUpdateInput(
                "X", "Y", null, null, null, null
        );

        when(hotelService.update(eq(999L), any(HotelUpdateInput.class)))
                .thenThrow(new EntityNotFoundException("Hotel with id 999 not found"));

        mockMvc.perform(put(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value("Not Found"))
                .andExpect(jsonPath("$.description").value("Hotel with id 999 not found"));
    }

    @Test
    @DisplayName("PUT /{hotelId} — 400 — name слишком длинный")
    void update_badRequest_nameTooLong() throws Exception {
        HotelUpdateInput input = new HotelUpdateInput(
                "A".repeat(51),
                "Desc", "Title", "City", "Addr", 100
        );

        mockMvc.perform(put(BASE_URL + PATH + HOTEL_ID_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    @DisplayName("DELETE /{hotelId} — 204 — отель успешно удалён")
    void delete_success() throws Exception {
        doNothing().when(hotelService).delete(HOTEL_ID_VALUE);

        mockMvc.perform(delete(BASE_URL + PATH + HOTEL_ID_VALUE))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /{hotelId} — 404 — отель для удаления не найден")
    void delete_notFound() throws Exception {
        doThrow(new EntityNotFoundException("Hotel with id 999 not found"))
                .when(hotelService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value("Not Found"))
                .andExpect(jsonPath("$.description").value("Hotel with id 999 not found"));
    }

    @Test
    @WithMockUser(username = "testUser")
    @DisplayName("POST /{hotelId}/grade — 200 — оценка успешно поставлена")
    void rate_success() throws Exception {
        Byte newMark = 5;
        GradeResponse expected = new GradeResponse(HOTEL_ID_VALUE, 5.0, 25.0, 5);

        when(hotelService.rate(eq(HOTEL_ID_VALUE), eq(newMark))).thenReturn(expected);

        mockMvc.perform(post(BASE_URL + "/{hotelId}/grade", HOTEL_ID_VALUE)
                        .param("newMark", newMark.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(5.0))
                .andExpect(jsonPath("$.totalRating").value(25.0))
                .andExpect(jsonPath("$.numberOfRating").value(5));

        verify(hotelService).rate(HOTEL_ID_VALUE, newMark);
    }

    @Test
    @WithMockUser(username = "testUser")
    @DisplayName("POST /{hotelId}/grade — 400 — оценка меньше 1")
    void rate_badRequest_markTooLow() throws Exception {
        Byte invalidMark = 0;

        mockMvc.perform(post(BASE_URL + "/{hotelId}/grade", HOTEL_ID_VALUE)
                        .param("newMark", invalidMark.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(hotelService, never()).rate(anyLong(), anyByte());
    }

    @Test
    @WithMockUser(username = "testUser")
    @DisplayName("POST /{hotelId}/grade — 400 — оценка больше 5")
    void rate_badRequest_markTooHigh() throws Exception {
        Byte invalidMark = 6;

        mockMvc.perform(post(BASE_URL + "/{hotelId}/grade", HOTEL_ID_VALUE)
                        .param("newMark", invalidMark.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(hotelService, never()).rate(anyLong(), anyByte());
    }

    @Test
    @DisplayName("POST /{hotelId}/grade — 400 — отсутствует параметр newMark")
    void rate_badRequest_missingMark() throws Exception {
        mockMvc.perform(post(BASE_URL + "/{hotelId}/grade", HOTEL_ID_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(hotelService, never()).rate(anyLong(), anyByte());
    }

    @Test
    @DisplayName("POST /hotel/filter — 200 — фильтрация по городу")
    void search_filterByCity() throws Exception {
        HotelResponse hotel1 = new HotelResponse(
                1L, "Hilton", "Luxury hotel", "Best Hotel", "Moscow", "Tverskaya 1", 1500, 4.5, 100.0, 20
        );

        var page = new PageImpl<>(
                List.of(hotel1),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id")),
                1
        );

        when(hotelService.getAllByFilter(any(HotelFilter.class), any(Pageable.class)))
                .thenReturn(page);

        HotelFilter filter = new HotelFilter("Moscow", null, null, null, null);

        mockMvc.perform(post(BASE_URL + FILTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].city").value("Moscow"));

        verify(hotelService).getAllByFilter(
                argThat(f -> f.city() != null && f.city().equals("Moscow")),
                any(Pageable.class)
        );
    }

    @Test
    @DisplayName("POST /hotel/filter — 200 — комбинированная фильтрация")
    void search_combinedFilter() throws Exception {
        Page<HotelResponse> page = new PageImpl<>(Collections.emptyList());

        when(hotelService.getAllByFilter(any(HotelFilter.class), any(Pageable.class)))
                .thenReturn(page);

        HotelFilter filter = new HotelFilter(
                "Moscow",
                "Hilton",
                500,
                5000,
                4.0
        );

        mockMvc.perform(post(BASE_URL + FILTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk());

        verify(hotelService).getAllByFilter(
                argThat(f ->
                        f.city() != null &&
                                f.nameContains() != null &&
                                f.minDistance() != null &&
                                f.maxDistance() != null &&
                                f.minRating() != null
                ),
                any(Pageable.class)
        );
    }

    @Test
    @DisplayName("POST /hotel/filter — 200 — пустой фильтр (все отели)")
    void search_emptyFilter() throws Exception {
        Page<HotelResponse> page = new PageImpl<>(Collections.emptyList());

        when(hotelService.getAllByFilter(any(HotelFilter.class), any(Pageable.class)))
                .thenReturn(page);

        HotelFilter filter = new HotelFilter(null, null, null, null, null);

        mockMvc.perform(post(BASE_URL + FILTER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk());

        verify(hotelService).getAllByFilter(
                argThat(f ->
                        f.city() == null &&
                                f.nameContains() == null &&
                                f.minDistance() == null &&
                                f.maxDistance() == null &&
                                f.minRating() == null
                ),
                any(Pageable.class)
        );
    }

}