package ru.moskalev.hotel_reservation.service;


import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.moskalev.hotel_reservation.domain.Grade;
import ru.moskalev.hotel_reservation.domain.Hotel;
import ru.moskalev.hotel_reservation.dto.grade.GradeResponse;
import ru.moskalev.hotel_reservation.dto.hotel.HotelCreateInput;
import ru.moskalev.hotel_reservation.dto.hotel.HotelResponse;
import ru.moskalev.hotel_reservation.dto.hotel.HotelUpdateInput;
import ru.moskalev.hotel_reservation.exception.EntityNotFoundException;
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
        HotelCreateInput input = getHotelCreateInput();

        // when
        HotelResponse response = hotelService.create(input);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo(input.name());
        assertThat(response.description()).isEqualTo(input.description());
        assertThat(response.title()).isEqualTo(input.title());
        assertThat(response.city()).isEqualTo(input.city());
        assertThat(response.distance()).isEqualTo(input.distance());
        assertThat(response.rating()).isZero();
        assertThat(response.totalRating()).isZero();
        assertThat(response.numberOfRating()).isZero();

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
                "ul Pushkina",
                0
        );

        // when
        HotelResponse response = hotelService.create(input);

        // then
        assertThat(response.name()).isEqualTo("Simple Hotel");
        assertThat(response.description()).isNull();
        assertThat(response.title()).isNull();
        assertThat(response.distance()).isZero();
        assertThat(response.rating()).isZero();
        assertThat(response.totalRating()).isZero();
        assertThat(response.numberOfRating()).isZero();
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
        assertThat(response.name()).isEqualTo(saved.getName());
    }

    @Test
    @DisplayName("getById: должен выбросить HotelException при несуществующем ID")
    void getById_shouldThrowWhenNotFound() {
        // given
        Long nonExistentId = 99999L;

        // when & then
        assertThatThrownBy(() -> hotelService.getById(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
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
        assertThat(response.name()).isEqualTo(input.name());
        assertThat(response.description()).isEqualTo(input.description());

        assertThat(response.title()).isEqualTo(saved.getTitle());
        assertThat(response.city()).isEqualTo(saved.getCity());
        assertThat(response.distance()).isEqualTo(saved.getDistance());
        assertThat(response.rating()).isZero();
        assertThat(response.totalRating()).isZero();
        assertThat(response.numberOfRating()).isZero();
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
        assertThat(response.name()).isEqualTo(saved.getName());
        assertThat(response.title()).isEqualTo(saved.getTitle());
        assertThat(response.city()).isEqualTo(saved.getCity());
        assertThat(response.rating()).isZero();
        assertThat(response.totalRating()).isZero();
        assertThat(response.numberOfRating()).isZero();
    }

    @Test
    @DisplayName("update: должен выбросить исключение при обновлении несуществующего отеля")
    void update_shouldThrowWhenNotFound() {
        // given
        HotelUpdateInput input = new HotelUpdateInput("New", null, null, null, null, null);

        // when & then
        assertThatThrownBy(() -> hotelService.update(99999L, input))
                .isInstanceOf(EntityNotFoundException.class);
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

    @Test
    @DisplayName("rate: должен добавить первую оценку и вернуть корректные значения")
    void rate_shouldAddFirstRatingAndReturnCorrectValues() {
        // given
        HotelCreateInput input = getHotelCreateInput();
        HotelResponse hotel = hotelService.create(input);
        Byte newMark = 5;

        // when
        GradeResponse response = hotelService.rate(hotel.id(), newMark);

        // then
        assertThat(response).isNotNull();
        assertThat(response.hotelId()).isEqualTo(hotel.id());
        assertThat(response.rating()).isEqualTo(5.0);
        assertThat(response.totalRating()).isEqualTo(5.0);
        assertThat(response.numberOfRating()).isEqualTo(1);
    }

    @Test
    @DisplayName("rate: должен корректно вычислять средний рейтинг при нескольких оценках")
    void rate_shouldCalculateAverageRatingCorrectly() {
        // given
        HotelCreateInput input = getHotelCreateInput();
        HotelResponse hotel = hotelService.create(input);

        // when
        hotelService.rate(hotel.id(), (byte) 5);
        hotelService.rate(hotel.id(), (byte) 4);
        GradeResponse response = hotelService.rate(hotel.id(), (byte) 3);

        // then
        assertThat(response.rating()).isEqualTo(4.0);
        assertThat(response.totalRating()).isEqualTo(12.0);
        assertThat(response.numberOfRating()).isEqualTo(3);
    }

    @Test
    @DisplayName("rate: должен округлять рейтинг до одного знака после запятой")
    void rate_shouldRoundRatingToOneDecimalPlace() {
        // given
        HotelCreateInput input = getHotelCreateInput();
        HotelResponse hotel = hotelService.create(input);

        // when
        hotelService.rate(hotel.id(), (byte) 5);
        hotelService.rate(hotel.id(), (byte) 4);
        GradeResponse response = hotelService.rate(hotel.id(), (byte) 4);

        // then
        assertThat(response.rating()).isEqualTo(4.3);
        assertThat(response.totalRating()).isEqualTo(13.0);
        assertThat(response.numberOfRating()).isEqualTo(3);
    }

    @Test
    @DisplayName("rate: должен корректно обрабатывать граничные значения оценок (1 и 5)")
    void rate_shouldHandleBoundaryValues() {
        // given
        HotelCreateInput input = getHotelCreateInput();
        HotelResponse hotel = hotelService.create(input);

        // when
        GradeResponse response1 = hotelService.rate(hotel.id(), (byte) 1);

        // then
        assertThat(response1.rating()).isEqualTo(1.0);
        assertThat(response1.totalRating()).isEqualTo(1.0);
        assertThat(response1.numberOfRating()).isEqualTo(1);

        // when
        GradeResponse response2 = hotelService.rate(hotel.id(), (byte) 5);

        // then
        assertThat(response2.rating()).isEqualTo(3.0);
        assertThat(response2.totalRating()).isEqualTo(6.0);
        assertThat(response2.numberOfRating()).isEqualTo(2);
    }

    @Test
    @DisplayName("rate: должен накапливать оценки и обновлять все поля корректно")
    void rate_shouldAccumulateRatingsAndPersistChanges() {
        // given
        HotelCreateInput input = getHotelCreateInput();
        HotelResponse hotel = hotelService.create(input);

        // when
        hotelService.rate(hotel.id(), (byte) 5);
        hotelService.rate(hotel.id(), (byte) 5);
        hotelService.rate(hotel.id(), (byte) 4);
        hotelService.rate(hotel.id(), (byte) 3);
        GradeResponse response = hotelService.rate(hotel.id(), (byte) 4);

        // then
        assertThat(response.rating()).isEqualTo(4.2);
        assertThat(response.totalRating()).isEqualTo(21.0);
        assertThat(response.numberOfRating()).isEqualTo(5);

        Hotel savedHotel = hotelRepository.findById(hotel.id()).orElseThrow();
        assertThat(savedHotel.getGrade().getRating()).isEqualTo(4.2);
        assertThat(savedHotel.getGrade().getTotalRating()).isEqualTo(21.0);
        assertThat(savedHotel.getGrade().getNumberOfRating()).isEqualTo(5);
    }

    private static @NotNull HotelCreateInput getHotelCreateInput() {
        return new HotelCreateInput(
                "Accumulation Hotel",
                "Описание",
                "Заголовок",
                "Новосибирск",
                "Ул. Накопительная",
                1200
        );
    }

    @Test
    @DisplayName("rate: должен корректно округлять вверх при .5 и более")
    void rate_shouldRoundUpCorrectly() {
        // given
        HotelCreateInput input = getHotelCreateInput();
        HotelResponse hotel = hotelService.create(input);

        // when
        hotelService.rate(hotel.id(), (byte) 5);
        hotelService.rate(hotel.id(), (byte) 4);
        GradeResponse response = hotelService.rate(hotel.id(), (byte) 5);

        // then
        assertThat(response.rating()).isEqualTo(4.7);
        assertThat(response.totalRating()).isEqualTo(14.0);
        assertThat(response.numberOfRating()).isEqualTo(3);
    }

    private Hotel buildHotel(String name) {
        Hotel hotel = new Hotel();
        hotel.setName(name);
        hotel.setDescription("Description");
        hotel.setTitle("Title");
        hotel.setCity("City");
        hotel.setAddress("Ul Pushkina");
        hotel.setDistance(100);
        hotel.setGrade(new Grade(0D,0D,0));
        return hotel;
    }
}