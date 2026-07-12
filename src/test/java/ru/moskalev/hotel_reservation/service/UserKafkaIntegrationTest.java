package ru.moskalev.hotel_reservation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.moskalev.hotel_reservation.domain.Hotel;
import ru.moskalev.hotel_reservation.domain.Room;
import ru.moskalev.hotel_reservation.domain.StatEventDocument;
import ru.moskalev.hotel_reservation.domain.User;
import ru.moskalev.hotel_reservation.dto.booking.BookingCreateRequest;
import ru.moskalev.hotel_reservation.dto.hotel.HotelCreateInput;
import ru.moskalev.hotel_reservation.dto.user.UserCreateInput;
import ru.moskalev.hotel_reservation.enumeration.UserRole;
import ru.moskalev.hotel_reservation.exception.EntityNotFoundException;
import ru.moskalev.hotel_reservation.repo.*;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static ru.moskalev.hotel_reservation.TestConstants.ADDRESS;
import static ru.moskalev.hotel_reservation.service.BookingServiceTest.setAuthentication;
import static ru.moskalev.hotel_reservation.service.RoomServiceIntegrationTest.buildUser;
import static ru.moskalev.hotel_reservation.service.RoomServiceIntegrationTest.getRoom;

class UserKafkaIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private UserService userService;
    @Autowired
    private StatEventRepository statEventRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private HotelRepository hotelRepository;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private HotelService hotelService;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAllInBatch();
        statEventRepository.deleteAll();
        userRepository.deleteAllInBatch();
    }

    @Test
    void shouldCreateUserPublishEventAndConsumeToMongo() {
        // given
        UserCreateInput input = new UserCreateInput(
                "testLogin",
                "test@mail.dev",
                "password123",
                UserRole.CLIENT
        );

        var savedUser = userService.create(input);

        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    List<StatEventDocument> events = statEventRepository.findAll();
                    assertThat(events).hasSize(1);
                    assertNotNull(events);
                    var doc = events.getFirst();
                    assertEquals(savedUser.id(), doc.getUserId());
                });
    }

    @Test
    void shouldCreateBookingPublishEventAndConsumeToMongo() {
        User user = userRepository.save(buildUser("testUser"));
        setAuthentication(user.getId(), "testuser");

        Long hotelId = hotelService.create(new HotelCreateInput(
                        "Hotel", null, null, "City", ADDRESS, 0))
                .id();
        var savedRoom = roomRepository.save(buildRoom(hotelId, "Room 1"));

        BookingCreateRequest request = new BookingCreateRequest(
                savedRoom.getId(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                2
        );

        // when
        bookingService.create(request);

        // then
        await()
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertThat(statEventRepository.findAll()).hasSize(1);
                });
    }

    private Room buildRoom(Long hotelId, String name) {
        Room room = getRoom(name);

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found"));
        room.setHotel(hotel);
        return room;
    }
}