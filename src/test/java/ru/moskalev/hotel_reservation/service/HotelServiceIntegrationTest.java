package ru.moskalev.hotel_reservation.service;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.moskalev.hotel_reservation.domain.Hotel;
import ru.moskalev.hotel_reservation.dto.hotel.HotelCreateInput;
import ru.moskalev.hotel_reservation.dto.hotel.HotelResponse;
import ru.moskalev.hotel_reservation.dto.hotel.HotelUpdateInput;
import ru.moskalev.hotel_reservation.exception.HotelException;
import ru.moskalev.hotel_reservation.repo.HotelRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("HotelService")
class HotelServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private HotelService hotelService;

    @Autowired
    private HotelRepository hotelRepository;

    @AfterEach
    void cleanUp() {
        hotelRepository.deleteAll();
    }

    @Test
    @DisplayName("create: должен создать отель и вернуть корректный DTO")
    void create_shouldSaveAndReturnDto() {
        // given
        HotelCreateInput input = new HotelCreateInput(
                "Grand Hotel",
                "Уютный отель в центре",
                "Лучший отель",
                "Москва",
                "Ул Пушкина дом Колотушкина",
                1500
        );

        // when
        HotelResponse response = hotelService.create(input);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("Grand Hotel");
        assertThat(response.description()).isEqualTo("Уютный отель в центре");
        assertThat(response.title()).isEqualTo("Лучший отель");
        assertThat(response.city()).isEqualTo("Москва");
        assertThat(response.distance()).isEqualTo(1500);

        assertThat(hotelRepository.findById(response.id())).isPresent();
    }

    @Test
    @DisplayName("create: должен корректно обработать null в опциональных полях")
    void create_shouldHandleNullOptionalFields() {
        // given
        HotelCreateInput input = new HotelCreateInput(
                "Simple Hotel",
                null,
                null,
                "SPB",
                null,
                0
        );

        // when
        HotelResponse response = hotelService.create(input);

        // then
        assertThat(response.name()).isEqualTo("Simple Hotel");
        assertThat(response.description()).isNull();
        assertThat(response.title()).isNull();
        assertThat(response.distance()).isZero();
    }

    @Test
    @DisplayName("getById: должен вернуть отель по существующему ID")
    void getById_shouldReturnHotel() {
        // given
        Hotel saved = hotelRepository.save(buildHotel("Test Hotel"));

        // when
        HotelResponse response = hotelService.getById(saved.getId());

        // then
        assertThat(response.id()).isEqualTo(saved.getId());
        assertThat(response.name()).isEqualTo("Test Hotel");
    }

    @Test
    @DisplayName("getById: должен выбросить HotelException при несуществующем ID")
    void getById_shouldThrowWhenNotFound() {
        // given
        Long nonExistentId = 99999L;

        // when & then
        assertThatThrownBy(() -> hotelService.getById(nonExistentId))
                .isInstanceOf(HotelException.class)
                .hasMessageContaining(nonExistentId.toString());
    }


    @Test
    @DisplayName("update: должен обновить переданные поля")
    void update_shouldUpdateProvidedFields() {
        // given
        Hotel saved = hotelRepository.save(buildHotel("Old Name"));
        HotelUpdateInput input = new HotelUpdateInput(
                "New Name",
                "New Description",
                null,
                null,
                null,
                null
        );

        // when
        HotelResponse response = hotelService.update(saved.getId(), input);

        // then
        assertThat(response.id()).isEqualTo(saved.getId());
        assertThat(response.name()).isEqualTo("New Name");
        assertThat(response.description()).isEqualTo("New Description");

        assertThat(response.title()).isEqualTo(saved.getTitle());
        assertThat(response.city()).isEqualTo(saved.getCity());
        assertThat(response.distance()).isEqualTo(saved.getDistance());
    }

    @Test
    @DisplayName("update: не должен затирать поля null-значениями")
    void update_shouldNotOverwriteWithNull() {
        // given
        Hotel saved = buildHotel("Original");
        saved.setTitle("Original Title");
        saved.setCity("Original City");
        hotelRepository.save(saved);

        HotelUpdateInput input = new HotelUpdateInput(null, null, null, null, null, null);

        // when
        HotelResponse response = hotelService.update(saved.getId(), input);

        // then
        assertThat(response.name()).isEqualTo("Original");
        assertThat(response.title()).isEqualTo("Original Title");
        assertThat(response.city()).isEqualTo("Original City");
    }

    @Test
    @DisplayName("update: должен выбросить исключение при обновлении несуществующего отеля")
    void update_shouldThrowWhenNotFound() {
        // given
        HotelUpdateInput input = new HotelUpdateInput("New", null, null, null, null, null);

        // when & then
        assertThatThrownBy(() -> hotelService.update(99999L, input))
                .isInstanceOf(HotelException.class);
    }

    @Test
    @DisplayName("delete: должен удалить существующий отель")
    void delete_shouldRemoveExistingHotel() {
        // given
        Hotel saved = hotelRepository.save(buildHotel("To Delete"));
        Long id = saved.getId();
        assertThat(hotelRepository.existsById(id)).isTrue();

        // when
        hotelService.delete(id);

        // then
        assertThat(hotelRepository.existsById(id)).isFalse();
    }

    @Test
    @DisplayName("delete: должен молча завершиться при удалении несуществующего отеля")
    void delete_shouldNotThrowWhenNotFound() {
        // given
        Long nonExistentId = 99999L;
        assertThat(hotelRepository.existsById(nonExistentId)).isFalse();

        // when & then
        hotelService.delete(nonExistentId);
    }

    @Test
    @DisplayName("findAll: должен вернуть страницу с корректными метаданными и ограниченным количеством элементов")
    void getAll_shouldReturnPageWithCorrectMetadata() {
        // given
        hotelRepository.save(buildHotel("Hotel 1"));
        hotelRepository.save(buildHotel("Hotel 2"));
        hotelRepository.save(buildHotel("Hotel 3"));

        Pageable pageable = PageRequest.of(0, 2);

        // when
        Page<HotelResponse> result = hotelService.getAll(pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.isFirst()).isTrue();
    }

    @Test
    @DisplayName("findAll: должен корректно применять сортировку по названию")
    void getAll_shouldApplySorting() {
        // given
        String zHotel = "Z Hotel";
        String aHotel = "A Hotel";
        String mHotel = "M Hotel";
        hotelRepository.save(buildHotel(zHotel));

        hotelRepository.save(buildHotel(aHotel));

        hotelRepository.save(buildHotel(mHotel));

        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());

        // when
        Page<HotelResponse> result = hotelService.getAll(pageable);

        // then
        assertThat(result.getContent())
                .extracting(HotelResponse::name)
                .containsExactly(aHotel, mHotel, zHotel);
    }

    @Test
    @DisplayName("findAll: должен вернуть пустой список, если запрашиваемая страница за пределами")
    void getAll_shouldReturnEmptyContentWhenPageOutOfBounds() {
        // given
        hotelRepository.save(buildHotel("Hotel 1"));

        Pageable pageable = PageRequest.of(5, 10);

        // when
        Page<HotelResponse> result = hotelService.getAll(pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(5);
    }

    @Test
    @DisplayName("findAll: должен корректно вернуть вторую страницу")
    void getAll_shouldReturnSecondPage() {
        // given
        hotelRepository.save(buildHotel("Hotel A"));
        hotelRepository.save(buildHotel("Hotel B"));
        hotelRepository.save(buildHotel("Hotel C"));
        hotelRepository.save(buildHotel("Hotel D"));

        Pageable pageable = PageRequest.of(1, 2);

        // when
        Page<HotelResponse> result = hotelService.getAll(pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.isFirst()).isFalse();
        assertThat(result.isLast()).isTrue();
    }

    private Hotel buildHotel(String name) {
        Hotel hotel = new Hotel();
        hotel.setName(name);
        hotel.setDescription("Description");
        hotel.setTitle("Title");
        hotel.setCity("City");
        hotel.setDistance(100);
        return hotel;
    }
}