package ru.moskalev.hotel_reservation.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.moskalev.hotel_reservation.domain.Booking;
import ru.moskalev.hotel_reservation.domain.Hotel;
import ru.moskalev.hotel_reservation.domain.Room;
import ru.moskalev.hotel_reservation.domain.User;
import ru.moskalev.hotel_reservation.dto.hotel.HotelCreateInput;
import ru.moskalev.hotel_reservation.dto.room.RoomCreateInput;
import ru.moskalev.hotel_reservation.dto.room.RoomFilter;
import ru.moskalev.hotel_reservation.dto.room.RoomResponse;
import ru.moskalev.hotel_reservation.dto.room.RoomUpdateInput;
import ru.moskalev.hotel_reservation.exception.EntityNotFoundException;
import ru.moskalev.hotel_reservation.repo.BookingRepository;
import ru.moskalev.hotel_reservation.repo.HotelRepository;
import ru.moskalev.hotel_reservation.repo.RoomRepository;
import ru.moskalev.hotel_reservation.repo.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.moskalev.hotel_reservation.TestConstants.ADDRESS;
import static ru.moskalev.hotel_reservation.utils.TestUtils.*;

@DisplayName("RoomService")
class RoomServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private HotelService hotelService;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @AfterEach
    void cleanUp() {
        bookingRepository.deleteAllInBatch();
        roomRepository.deleteAllInBatch();
        hotelRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("create: должен создать номер и вернуть корректный DTO")
    void create_shouldSaveAndReturnDto() {
        // given
        Long hotelId = hotelService.create(new HotelCreateInput(
                "Grand Hotel", "Desc", "Title", "Москва", "Ул Пушкина", 1500
        )).id();

        RoomCreateInput input = new RoomCreateInput(
                "Люкс",
                "Просторный номер",
                (short) 101,
                new BigDecimal("5000.00"),
                (byte) 2,
                1700000000L,
                1700100000L
        );

        // when
        RoomResponse response = roomService.create(hotelId, input);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("Люкс");
        assertThat(response.description()).isEqualTo("Просторный номер");
        assertThat(response.number()).isEqualTo((short) 101);
        assertThat(response.price()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(response.maxCount()).isEqualTo((byte) 2);
        assertThat(response.hotelId()).isEqualTo(hotelId);

        assertThat(roomRepository.findById(response.id())).isPresent();
    }

    @Test
    @DisplayName("create: должен корректно обработать null в опциональных полях")
    void create_shouldHandleNullOptionalFields() {
        // given
        Long hotelId = hotelService.create(new HotelCreateInput(
                "Simple Hotel", null, null, "SPB", ADDRESS, 1
        )).id();

        RoomCreateInput input = new RoomCreateInput(
                "Standard",
                null,
                (short) 202,
                new BigDecimal("3000.00"),
                (byte) 1,
                1700000000L,
                1700100000L
        );

        // when
        RoomResponse response = roomService.create(hotelId, input);

        // then
        assertThat(response.name()).isEqualTo("Standard");
        assertThat(response.description()).isNull();
    }

    @Test
    @DisplayName("create: должен выбросить исключение при несуществующем отеле")
    void create_shouldThrowWhenHotelNotFound() {
        // given
        Long nonExistentHotelId = 99999L;
        RoomCreateInput input = new RoomCreateInput(
                "Room", null, (short) 1, new BigDecimal("1000.00"),
                (byte) 1, 1700000000L, 1700100000L
        );

        // when & then
        assertThatThrownBy(() -> roomService.create(nonExistentHotelId, input))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(nonExistentHotelId.toString());
    }

    @Test
    @DisplayName("getById: должен вернуть номер по существующему ID")
    void getById_shouldReturnRoom() {
        // given
        Long hotelId = hotelService.create(new HotelCreateInput(
                "Test Hotel", null, null, "City", ADDRESS, 1
        )).id();
        Room saved = roomRepository.save(buildRoom(hotelId, "Test Room"));

        // when
        RoomResponse response = roomService.getById(saved.getId());

        // then
        assertThat(response.id()).isEqualTo(saved.getId());
        assertThat(response.name()).isEqualTo("Test Room");
        assertThat(response.hotelId()).isEqualTo(hotelId);
    }

    @Test
    @DisplayName("getById: должен выбросить исключение при несуществующем ID")
    void getById_shouldThrowWhenNotFound() {
        // given
        Long nonExistentId = 99999L;

        // when & then
        assertThatThrownBy(() -> roomService.getById(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(nonExistentId.toString());
    }

    @Test
    @DisplayName("getAllByHotelId: должен вернуть страницу с корректными метаданными")
    void getAllByHotelId_shouldReturnPageWithCorrectMetadata() {
        // given
        Long hotelId = hotelService.create(new HotelCreateInput(
                "Hotel", null, null, "City", ADDRESS, 1
        )).id();
        roomRepository.save(buildRoom(hotelId, "Room 1"));
        roomRepository.save(buildRoom(hotelId, "Room 2"));
        roomRepository.save(buildRoom(hotelId, "Room 3"));

        Pageable pageable = PageRequest.of(0, 2);

        // when
        var result = roomService.getAllByHotelId(hotelId, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.isFirst()).isTrue();
    }

    @Test
    @DisplayName("getAllByHotelId: должен корректно применять сортировку по названию")
    void getAllByHotelId_shouldApplySorting() {
        // given
        Long hotelId = hotelService.create(new HotelCreateInput(
                "Hotel", null, null, "City", ADDRESS, 1
        )).id();
        roomRepository.save(buildRoom(hotelId, "Z Room"));
        roomRepository.save(buildRoom(hotelId, "A Room"));
        roomRepository.save(buildRoom(hotelId, "M Room"));

        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());

        // when
        var result = roomService.getAllByHotelId(hotelId, pageable);

        // then
        assertThat(result.getContent())
                .extracting(RoomResponse::name)
                .containsExactly("A Room", "M Room", "Z Room");
    }

    @Test
    @DisplayName("getAllByHotelId: должен вернуть пустой список, если страница за пределами")
    void getAllByHotelId_shouldReturnEmptyContentWhenPageOutOfBounds() {
        // given
        Long hotelId = hotelService.create(new HotelCreateInput(
                "Hotel", null, null, "City", ADDRESS, 1
        )).id();
        roomRepository.save(buildRoom(hotelId, "Room 1"));

        Pageable pageable = PageRequest.of(5, 10);

        // when
        var result = roomService.getAllByHotelId(hotelId, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(5);
    }

    @Test
    @DisplayName("getAllByHotelId: должен корректно вернуть вторую страницу")
    void getAllByHotelId_shouldReturnSecondPage() {
        // given
        Long hotelId = hotelService.create(new HotelCreateInput(
                "Hotel", null, null, "City", ADDRESS, 1
        )).id();
        roomRepository.save(buildRoom(hotelId, "Room A"));
        roomRepository.save(buildRoom(hotelId, "Room B"));
        roomRepository.save(buildRoom(hotelId, "Room C"));
        roomRepository.save(buildRoom(hotelId, "Room D"));

        Pageable pageable = PageRequest.of(1, 2);

        // when
        var result = roomService.getAllByHotelId(hotelId, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.isFirst()).isFalse();
        assertThat(result.isLast()).isTrue();
    }

    @Test
    @DisplayName("getAllByHotelId: должен выбросить исключение при несуществующем отеле")
    void getAllByHotelId_shouldThrowWhenHotelNotFound() {
        // given
        Long nonExistentHotelId = 99999L;
        Pageable pageable = PageRequest.of(0, 10);

        // when & then
        assertThatThrownBy(() -> roomService.getAllByHotelId(nonExistentHotelId, pageable))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(nonExistentHotelId.toString());
    }

    @Test
    @DisplayName("update: должен обновить переданные поля")
    void update_shouldUpdateProvidedFields() {
        // given
        Long hotelId = hotelService.create(new HotelCreateInput(
                "Hotel", null, null, "City", ADDRESS, 1
        )).id();
        Room saved = roomRepository.save(buildRoom(hotelId, "Old Name"));

        RoomUpdateInput input = new RoomUpdateInput(
                "New Name",
                "New Description",
                null,
                null,
                null,
                null,
                null
        );

        // when
        RoomResponse response = roomService.update(saved.getId(), input);

        // then
        assertThat(response.id()).isEqualTo(saved.getId());
        assertThat(response.name()).isEqualTo("New Name");
        assertThat(response.description()).isEqualTo("New Description");
        assertThat(response.number()).isEqualTo(saved.getNumber());
        assertThat(response.price()).isEqualByComparingTo(saved.getPrice());
        assertThat(response.hotelId()).isEqualTo(hotelId);
    }

    @Test
    @DisplayName("update: не должен затирать поля null-значениями")
    void update_shouldNotOverwriteWithNull() {
        // given
        Long hotelId = hotelService.create(new HotelCreateInput(
                "Hotel", null, null, "City", ADDRESS, 1
        )).id();
        Room saved = buildRoom(hotelId, "Original");
        saved.setDescription("Original Description");
        saved.setNumber((short) 999);
        saved.setPrice(new BigDecimal("7777.00"));
        roomRepository.save(saved);

        RoomUpdateInput input = new RoomUpdateInput(null, null, null, null, null, null, null);

        // when
        RoomResponse response = roomService.update(saved.getId(), input);

        // then
        assertThat(response.name()).isEqualTo("Original");
        assertThat(response.description()).isEqualTo("Original Description");
        assertThat(response.number()).isEqualTo((short) 999);
        assertThat(response.price()).isEqualByComparingTo(new BigDecimal("7777.00"));
    }

    @Test
    @DisplayName("update: должен выбросить исключение при обновлении несуществующего номера")
    void update_shouldThrowWhenNotFound() {
        // given
        RoomUpdateInput input = new RoomUpdateInput("New", null, null, null, null, null, null);

        // when & then
        assertThatThrownBy(() -> roomService.update(99999L, input))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("delete: должен удалить существующий номер")
    void delete_shouldRemoveExistingRoom() {
        // given
        Long hotelId = hotelService.create(new HotelCreateInput(
                "Hotel", null, null, "City", ADDRESS, 1
        )).id();
        Room saved = roomRepository.save(buildRoom(hotelId, "To Delete"));
        Long id = saved.getId();
        assertThat(roomRepository.existsById(id)).isTrue();

        // when
        roomService.delete(id);

        // then
        assertThat(roomRepository.existsById(id)).isFalse();
    }

    @Test
    @DisplayName("должен вернуть все комнаты отеля при пустом фильтре")
    void getAllByHotelId_shouldReturnAllRooms_whenFilterIsEmpty() {
        // given
        Hotel hotel = hotelRepository.save(buildHotel("Test Hotel"));
        Room room1 = buildRoom("Room 1", hotel, (short) 1, BigDecimal.valueOf(1000), (byte) 2);
        Room room2 = buildRoom("Room 2", hotel, (short) 2, BigDecimal.valueOf(2000), (byte) 4);
        roomRepository.saveAll(List.of(room1, room2));

        RoomFilter filter = new RoomFilter(null, null, null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        var result = roomService.getAllByHotelIdAndFilter(hotel.getId(), filter, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("должен фильтровать по диапазону цен")
    void getAllByHotelId_shouldFilterByPriceRange() {
        // given
        Hotel hotel = hotelRepository.save(buildHotel("Test Hotel"));
        Room room1 = buildRoom("Cheap", hotel, (short) 1, BigDecimal.valueOf(500), (byte) 2);
        Room room2 = buildRoom("Medium", hotel, (short) 2, BigDecimal.valueOf(2000), (byte) 2);
        Room room3 = buildRoom("Expensive", hotel, (short) 3, BigDecimal.valueOf(5000), (byte) 2);
        roomRepository.saveAll(List.of(room1, room2, room3));

        RoomFilter filter = new RoomFilter(null, null, BigDecimal.valueOf(1000), BigDecimal.valueOf(3000), null, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        var result = roomService.getAllByHotelIdAndFilter(hotel.getId(), filter, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().name()).isEqualTo("Medium");
    }

    @Test
    @DisplayName("должен фильтровать по вместимости")
    void getAllByHotelId_shouldFilterByGuestCount() {
        // given
        Hotel hotel = hotelRepository.save(buildHotel("Test Hotel"));
        Room room1 = buildRoom("Small", hotel, (short) 1, BigDecimal.valueOf(1000), (byte) 1);
        Room room2 = buildRoom("Big", hotel, (short) 2, BigDecimal.valueOf(2000), (byte) 4);
        roomRepository.saveAll(List.of(room1, room2));

        RoomFilter filter = new RoomFilter(null, null, null, null, 2, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        var result = roomService.getAllByHotelIdAndFilter(hotel.getId(), filter, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().name()).isEqualTo("Big");
    }

    @Test
    @DisplayName("должен вернуть только свободные комнаты на указанные даты")
    void getAllByHotelId_shouldReturnOnlyFreeRoomsForDates() {
        // given
        Hotel hotel = hotelRepository.save(buildHotel("Test Hotel"));
        Room room1 = buildRoom("Room 1", hotel, (short) 1, BigDecimal.valueOf(1000), (byte) 2);
        Room room2 = buildRoom("Room 2", hotel, (short) 2, BigDecimal.valueOf(2000), (byte) 2);
        roomRepository.saveAll(List.of(room1, room2));

        User user = userRepository.save(buildUser("testUser"));

        // Бронирование для room1: 10 июля - 15 июля 2026
        long bookingStart = LocalDate.of(2026, 7, 10).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        long bookingEnd = LocalDate.of(2026, 7, 15).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        Booking booking = new Booking(null, bookingStart, bookingEnd, 2, room1, user);
        bookingRepository.save(booking);

        // Запрос на 12 июля - 14 июля (пересекается с бронированием)
        long filterStart = LocalDate.of(2026, 7, 12).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        long filterEnd = LocalDate.of(2026, 7, 14).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();

        RoomFilter filter = new RoomFilter(null, null, null, null, null, filterStart, filterEnd);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        var result = roomService.getAllByHotelIdAndFilter(hotel.getId(), filter, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().name()).isEqualTo("Room 2");
    }

    @Test
    @DisplayName("не должен применять фильтр по датам, если указано только одно поле")
    void getAllByHotelId_shouldNotFilterByDates_whenOnlyOneDateProvided() {
        // given
        Hotel hotel = hotelRepository.save(buildHotel("Test Hotel"));
        Room room1 = buildRoom("Room 1", hotel, (short) 1, BigDecimal.valueOf(1000), (byte) 2);
        roomRepository.save(room1);

        long filterStart = LocalDate.of(2026, 7, 12).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();

        // Только startDate, без endDate
        RoomFilter filter = new RoomFilter(null, null, null, null, null, filterStart, null);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        var result = roomService.getAllByHotelIdAndFilter(hotel.getId(), filter, pageable);

        // then фильтр по датам не сработал
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    private Room buildRoom(Long hotelId, String name) {
        Room room = getRoom(name);

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found"));
        room.setHotel(hotel);
        return room;
    }

    public static Room buildRoom(String name, Hotel hotel, short number, BigDecimal price, byte maxCount) {
        Room room = new Room();
        room.setName(name);
        room.setDescription("Description for " + name);
        room.setNumber(number);
        room.setPrice(price);
        room.setMaxCount(maxCount);
        room.setHotel(hotel);
        return room;
    }
}