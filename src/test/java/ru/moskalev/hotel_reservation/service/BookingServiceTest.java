package ru.moskalev.hotel_reservation.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.moskalev.hotel_reservation.domain.Booking;
import ru.moskalev.hotel_reservation.domain.CustomUserDetails;
import ru.moskalev.hotel_reservation.dto.booking.BookingCreateRequest;
import ru.moskalev.hotel_reservation.dto.booking.BookingResponse;
import ru.moskalev.hotel_reservation.dto.hotel.HotelCreateInput;
import ru.moskalev.hotel_reservation.dto.room.RoomCreateInput;
import ru.moskalev.hotel_reservation.dto.room.RoomResponse;
import ru.moskalev.hotel_reservation.dto.user.UserCreateInput;
import ru.moskalev.hotel_reservation.dto.user.UserResponse;
import ru.moskalev.hotel_reservation.enumeration.UserRole;
import ru.moskalev.hotel_reservation.exception.BookingException;
import ru.moskalev.hotel_reservation.exception.EntityNotFoundException;
import ru.moskalev.hotel_reservation.repo.BookingRepository;
import ru.moskalev.hotel_reservation.repo.HotelRepository;
import ru.moskalev.hotel_reservation.repo.RoomRepository;
import ru.moskalev.hotel_reservation.repo.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("BookingService")
class BookingServiceTest extends BaseIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private HotelService hotelService;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private Long testRoomId;
    private Long testUserId;

    @BeforeEach
    void setUp() {
        Long testHotelId = hotelService.create(new HotelCreateInput(
                "Test Hotel", "Description", "Title", "Москва", "Ул Тестовая", 1000
        )).id();

        RoomResponse room = roomService.create(testHotelId, new RoomCreateInput(
                "Test Room",
                "Test Description",
                (short) 101,
                new BigDecimal("3000.00"),
                (byte) 3,
                1700000000L,
                1700100000L
        ));
        testRoomId = room.id();

        UserResponse user = userService.create(new UserCreateInput(
                "testuser", "test@example.com", "password123", UserRole.ADMIN
        ));
        testUserId = user.id();

        setAuthentication(testUserId, "testuser");
    }

    @AfterEach
    void cleanUp() {
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
        hotelRepository.deleteAll();
        userRepository.deleteAll();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("create: должен создать бронирование и вернуть корректный DTO")
    void create_shouldCreateBookingAndReturnResponse() {
        // given
        BookingCreateRequest request = new BookingCreateRequest(
                testRoomId,
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 15),
                2
        );

        // when
        BookingResponse response = bookingService.create(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.roomId()).isEqualTo(testRoomId);
        assertThat(response.userId()).isEqualTo(testUserId);
        assertThat(response.guestCount()).isEqualTo(2);
        assertThat(response.startDate()).isEqualTo(LocalDate.of(2026, 8, 10));
        assertThat(response.endDate()).isEqualTo(LocalDate.of(2026, 8, 15));

        assertThat(bookingRepository.findById(response.id())).isPresent();
    }

    @Test
    @DisplayName("create: должен выбросить исключение при превышении вместимости")
    void create_shouldThrowExceptionWhenTooManyGuests() {
        // given
        BookingCreateRequest request = new BookingCreateRequest(
                testRoomId,
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 15),
                5
        );

        // when & then
        assertThatThrownBy(() -> bookingService.create(request))
                .isInstanceOf(BookingException.class)
                .hasMessageContaining("Too many guests");
    }

    @Test
    @DisplayName("create: должен выбросить исключение при пересечении дат")
    void create_shouldThrowExceptionWhenDatesOverlap() {
        // given
        BookingCreateRequest firstBooking = new BookingCreateRequest(
                testRoomId,
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 15),
                2
        );
        bookingService.create(firstBooking);

        // пытаемся создать вторую бронь на пересекающиеся даты
        BookingCreateRequest overlappingBooking = new BookingCreateRequest(
                testRoomId,
                LocalDate.of(2026, 8, 12),
                LocalDate.of(2026, 8, 14),
                1
        );

        // when & then
        assertThatThrownBy(() -> bookingService.create(overlappingBooking))
                .isInstanceOf(BookingException.class)
                .hasMessageContaining("already booked");
    }

    @Test
    @DisplayName("create: должен позволить бронировать на непересекающиеся даты")
    void create_shouldAllowBookingOnNonOverlappingDates() {
        // given
        BookingCreateRequest firstBooking = new BookingCreateRequest(
                testRoomId,
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 15),
                2
        );
        bookingService.create(firstBooking);

        // создаем вторую бронь на другие даты
        BookingCreateRequest secondBooking = new BookingCreateRequest(
                testRoomId,
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 8, 25),
                1
        );

        // when
        BookingResponse response = bookingService.create(secondBooking);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(bookingRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("create: должен выбросить исключение если комната не найдена")
    void create_shouldThrowExceptionWhenRoomNotFound() {
        // given
        BookingCreateRequest request = new BookingCreateRequest(
                99999L,
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 15),
                2
        );

        // when & then
        assertThatThrownBy(() -> bookingService.create(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("getById: должен вернуть бронирование по ID")
    void getById_shouldReturnBooking() {
        // given - создаем бронь
        BookingCreateRequest request = new BookingCreateRequest(
                testRoomId,
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 15),
                2
        );
        BookingResponse created = bookingService.create(request);

        // when
        BookingResponse response = bookingService.getById(created.id());

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(created.id());
        assertThat(response.roomId()).isEqualTo(testRoomId);
        assertThat(response.userId()).isEqualTo(testUserId);
    }

    @Test
    @DisplayName("getById: должен выбросить исключение если бронь не найдена")
    void getById_shouldThrowExceptionWhenNotFound() {
        // when & then
        assertThatThrownBy(() -> bookingService.getById(99999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("getMyBookings: должен вернуть все бронирования текущего пользователя")
    void getMyBookings_shouldReturnUserBookings() {
        // given - создаем несколько броней
        bookingService.create(new BookingCreateRequest(
                testRoomId,
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 15),
                2
        ));

        bookingService.create(new BookingCreateRequest(
                testRoomId,
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 8, 25),
                1
        ));

        // when
        List<BookingResponse> bookings = bookingService.getMyBookings();

        // then
        assertThat(bookings)
                .hasSize(2)
                .allMatch(b -> b.userId().equals(testUserId));

    }

    @Test
    @DisplayName("getMyBookings: должен вернуть пустой список если нет броней")
    void getMyBookings_shouldReturnEmptyListWhenNoBookings() {
        // when
        List<BookingResponse> bookings = bookingService.getMyBookings();

        // then
        assertThat(bookings).isEmpty();
    }

    @Test
    @DisplayName("cancel: должен удалить бронирование")
    void cancel_shouldDeleteBooking() {
        // given - создаем бронь
        BookingCreateRequest request = new BookingCreateRequest(
                testRoomId,
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 15),
                2
        );
        BookingResponse created = bookingService.create(request);

        // when
        bookingService.cancel(created.id());

        // then
        assertThat(bookingRepository.findById(created.id())).isEmpty();
    }

    @Test
    @DisplayName("cancel: должен выбросить исключение если бронь не найдена")
    void cancel_shouldThrowExceptionWhenNotFound() {
        // when & then
        assertThatThrownBy(() -> bookingService.cancel(99999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("cancel: должен выбросить исключение при попытке отменить чужую бронь")
    void cancel_shouldThrowExceptionWhenNotOwner() {
        // given
        UserResponse otherUser = userService.create(new UserCreateInput(
                "otheruser", "other@example.com", "password123", UserRole.CLIENT
        ));

        // создаем бронь от имени другого пользователя
        setAuthentication(otherUser.id(), "otheruser");
        BookingResponse otherBooking = bookingService.create(new BookingCreateRequest(
                testRoomId,
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 15),
                2
        ));

        // переключаемся обратно на testUser
        setAuthentication(testUserId, "testuser");

        // when & then - пытаемся отменить чужую бронь
        assertThatThrownBy(() -> bookingService.cancel(otherBooking.id()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("only cancel your own");
    }

    @Test
    @DisplayName("create: должен корректно конвертировать LocalDate в long")
    void create_shouldConvertLocalDateToLong() {
        // given
        LocalDate startDate = LocalDate.of(2026, 8, 10);
        LocalDate endDate = LocalDate.of(2026, 8, 15);

        BookingCreateRequest request = new BookingCreateRequest(
                testRoomId,
                startDate,
                endDate,
                2
        );

        // when
        BookingResponse response = bookingService.create(request);

        // then
        assertThat(response.startDate()).isEqualTo(startDate);
        assertThat(response.endDate()).isEqualTo(endDate);

        Booking booking = bookingRepository.findById(response.id()).orElseThrow();
        assertThat(booking.getStartDate()).isGreaterThan(0);
        assertThat(booking.getEndDate()).isGreaterThan(booking.getStartDate());
    }

    /**
     * Вспомогательный метод для установки аутентификации в тестах
     */
    private void setAuthentication(Long userId, String username) {
        CustomUserDetails userDetails = new CustomUserDetails(
                userId,
                username,
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}