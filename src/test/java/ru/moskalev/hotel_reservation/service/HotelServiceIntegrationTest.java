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
import ru.moskalev.hotel_reservation.dto.hotel.HotelFilter;
import ru.moskalev.hotel_reservation.dto.hotel.HotelResponse;
import ru.moskalev.hotel_reservation.dto.hotel.HotelUpdateInput;
import ru.moskalev.hotel_reservation.exception.EntityNotFoundException;
import ru.moskalev.hotel_reservation.repo.HotelRepository;

import java.util.List;

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

    @Test
    @DisplayName("getAllByFilter: должен вернуть все отели при пустом фильтре")
    void getAllByFilter_shouldReturnAllHotels_whenFilterIsEmpty() {
        // given
        Hotel hotel1 = buildHotel("Hilton Moscow");
        hotel1.setCity("Moscow");
        Hotel hotel2 = buildHotel("Marriott SPB");
        hotel2.setCity("SPB");
        Hotel hotel3 = buildHotel("Radisson Moscow");
        hotel3.setCity("Moscow");

        hotelRepository.saveAll(List.of(hotel1, hotel2, hotel3));

        HotelFilter filter = new HotelFilter(null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));

        // when
        Page<HotelResponse> result = hotelService.getAllByFilter(filter, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    @DisplayName("getAllByFilter: должен фильтровать по городу")
    void getAllByFilter_shouldFilterByCity() {
        // given
        Hotel hotel1 = buildHotel("Hilton Moscow");
        hotel1.setCity("Moscow");
        Hotel hotel2 = buildHotel("Marriott SPB");
        hotel2.setCity("SPB");
        Hotel hotel3 = buildHotel("Radisson Moscow");
        hotel3.setCity("Moscow");

        hotelRepository.saveAll(List.of(hotel1, hotel2, hotel3));

        HotelFilter filter = new HotelFilter("Moscow", null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<HotelResponse> result = hotelService.getAllByFilter(filter, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .allSatisfy(hotel -> assertThat(hotel.city()).isEqualTo("Moscow"));
    }

    @Test
    @DisplayName("getAllByFilter: должен фильтровать по части названия (nameContains)")
    void getAllByFilter_shouldFilterByNameContains() {
        // given
        Hotel hotel1 = buildHotel("Grand Hotel Moscow");
        hotel1.setCity("Moscow");
        Hotel hotel2 = buildHotel("Marriott SPB");
        hotel2.setCity("SPB");
        Hotel hotel3 = buildHotel("Grand Palace");
        hotel3.setCity("Moscow");

        hotelRepository.saveAll(List.of(hotel1, hotel2, hotel3));

        HotelFilter filter = new HotelFilter(null, "Grand", null, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<HotelResponse> result = hotelService.getAllByFilter(filter, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .allSatisfy(hotel -> assertThat(hotel.name()).containsIgnoringCase("Grand"));
    }

    @Test
    @DisplayName("getAllByFilter: должен фильтровать по минимальному расстоянию")
    void getAllByFilter_shouldFilterByMinDistance() {
        // given
        Hotel hotel1 = buildHotel("Hotel Near");
        hotel1.setDistance(100);
        Hotel hotel2 = buildHotel("Hotel Medium");
        hotel2.setDistance(500);
        Hotel hotel3 = buildHotel("Hotel Far");
        hotel3.setDistance(1000);

        hotelRepository.saveAll(List.of(hotel1, hotel2, hotel3));

        HotelFilter filter = new HotelFilter(null, null, 500, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<HotelResponse> result = hotelService.getAllByFilter(filter, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .allSatisfy(hotel -> assertThat(hotel.distance()).isGreaterThanOrEqualTo(500));
    }

    @Test
    @DisplayName("getAllByFilter: должен фильтровать по максимальному расстоянию")
    void getAllByFilter_shouldFilterByMaxDistance() {
        // given
        Hotel hotel1 = buildHotel("Hotel Near");
        hotel1.setDistance(100);
        Hotel hotel2 = buildHotel("Hotel Medium");
        hotel2.setDistance(500);
        Hotel hotel3 = buildHotel("Hotel Far");
        hotel3.setDistance(1000);

        hotelRepository.saveAll(List.of(hotel1, hotel2, hotel3));

        HotelFilter filter = new HotelFilter(null, null, null, 500, null);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<HotelResponse> result = hotelService.getAllByFilter(filter, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .allSatisfy(hotel -> assertThat(hotel.distance()).isLessThanOrEqualTo(500));
    }

    @Test
    @DisplayName("getAllByFilter: должен фильтровать по диапазону расстояний")
    void getAllByFilter_shouldFilterByDistanceRange() {
        // given
        Hotel hotel1 = buildHotel("Hotel Near");
        hotel1.setDistance(100);
        Hotel hotel2 = buildHotel("Hotel Medium");
        hotel2.setDistance(500);
        Hotel hotel3 = buildHotel("Hotel Far");
        hotel3.setDistance(1000);

        hotelRepository.saveAll(List.of(hotel1, hotel2, hotel3));

        HotelFilter filter = new HotelFilter(null, null, 200, 800, null);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<HotelResponse> result = hotelService.getAllByFilter(filter, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Hotel Medium");
        assertThat(result.getContent().get(0).distance()).isEqualTo(500);
    }

    @Test
    @DisplayName("getAllByFilter: должен фильтровать по минимальному рейтингу")
    void getAllByFilter_shouldFilterByMinRating() {
        // given
        Hotel hotel1 = buildHotel("Hotel Low Rating");
        hotel1.setGrade(new Grade(2.0, 10.0, 5));
        Hotel hotel2 = buildHotel("Hotel Medium Rating");
        hotel2.setGrade(new Grade(4.0, 20.0, 5));
        Hotel hotel3 = buildHotel("Hotel High Rating");
        hotel3.setGrade(new Grade(4.8, 24.0, 5));

        hotelRepository.saveAll(List.of(hotel1, hotel2, hotel3));

        HotelFilter filter = new HotelFilter(null, null, null, null, 4.0);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<HotelResponse> result = hotelService.getAllByFilter(filter, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .allSatisfy(hotel -> assertThat(hotel.rating()).isGreaterThanOrEqualTo(4.0));
    }

    @Test
    @DisplayName("getAllByFilter: должен применять комбинированную фильтрацию")
    void getAllByFilter_shouldApplyCombinedFilter() {
        // given
        Hotel hotel1 = buildHotel("Grand Hotel Moscow");
        hotel1.setCity("Moscow");
        hotel1.setDistance(500);
        hotel1.setGrade(new Grade(4.5, 45.0, 10));

        Hotel hotel2 = buildHotel("Marriott SPB");
        hotel2.setCity("SPB");
        hotel2.setDistance(300);
        hotel2.setGrade(new Grade(4.2, 42.0, 10));

        Hotel hotel3 = buildHotel("Grand Palace Moscow");
        hotel3.setCity("Moscow");
        hotel3.setDistance(1000);
        hotel3.setGrade(new Grade(3.5, 35.0, 10));

        hotelRepository.saveAll(List.of(hotel1, hotel2, hotel3));

        HotelFilter filter = new HotelFilter("Moscow", "Grand", 200, 800, 4.0);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<HotelResponse> result = hotelService.getAllByFilter(filter, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Grand Hotel Moscow");
        assertThat(result.getContent().get(0).city()).isEqualTo("Moscow");
        assertThat(result.getContent().get(0).distance()).isEqualTo(500);
        assertThat(result.getContent().get(0).rating()).isEqualTo(4.5);
    }

    @Test
    @DisplayName("getAllByFilter: должен работать с пагинацией")
    void getAllByFilter_shouldWorkWithPagination() {
        // given
        for (int i = 1; i <= 15; i++) {
            Hotel hotel = buildHotel("Hotel " + i);
            hotel.setCity("Moscow");
            hotelRepository.save(hotel);
        }

        HotelFilter filter = new HotelFilter("Moscow", null, null, null, null);
        Pageable pageable = PageRequest.of(0, 5);

        // when
        Page<HotelResponse> result = hotelService.getAllByFilter(filter, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(15);
        assertThat(result.getContent()).hasSize(5);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getNumber()).isZero();
    }

    @Test
    @DisplayName("getAllByFilter: должен вернуть пустую страницу, если ничего не найдено")
    void getAllByFilter_shouldReturnEmptyPage_whenNoResults() {
        // given
        Hotel hotel1 = buildHotel("Hilton Moscow");
        hotel1.setCity("Moscow");
        Hotel hotel2 = buildHotel("Marriott SPB");
        hotel2.setCity("SPB");

        hotelRepository.saveAll(List.of(hotel1, hotel2));

        HotelFilter filter = new HotelFilter("Kazan", null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<HotelResponse> result = hotelService.getAllByFilter(filter, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }



    public static Hotel buildHotel(String name) {
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