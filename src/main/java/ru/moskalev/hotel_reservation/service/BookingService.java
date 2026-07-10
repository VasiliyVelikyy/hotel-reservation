package ru.moskalev.hotel_reservation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.moskalev.hotel_reservation.domain.Booking;
import ru.moskalev.hotel_reservation.domain.CustomUserDetails;
import ru.moskalev.hotel_reservation.domain.Room;
import ru.moskalev.hotel_reservation.domain.User;
import ru.moskalev.hotel_reservation.dto.booking.BookingCreateRequest;
import ru.moskalev.hotel_reservation.dto.booking.BookingResponse;
import ru.moskalev.hotel_reservation.dto.kafka.RoomBookedEvent;
import ru.moskalev.hotel_reservation.exception.BookingException;
import ru.moskalev.hotel_reservation.exception.EntityNotFoundException;
import ru.moskalev.hotel_reservation.integration.kafka.KafkaStatsPublisher;
import ru.moskalev.hotel_reservation.mapper.BookingMapper;
import ru.moskalev.hotel_reservation.repo.BookingRepository;

import java.util.List;
import java.util.Objects;

import static ru.moskalev.hotel_reservation.exception.ErrorMessagesTemplates.*;
import static ru.moskalev.hotel_reservation.utils.CommonUtil.toEpochSecond;
import static ru.moskalev.hotel_reservation.utils.CommonUtil.toLocalDate;


@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {
    private final RoomService roomService;
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final BookingMapper bookingMapper;
    private final KafkaStatsPublisher kafkaStatsPublisher;

    @Transactional
    public BookingResponse create(BookingCreateRequest request) {
        Room room = roomService.findByIdForUpdate(request.roomId());

        if (request.guestCount() > room.getMaxCount()) {
            throw new BookingException(BOOKING_TOO_MANY_GUESTS_TEMPLATE.formatted(request.guestCount()));
        }

        long reqStart = toEpochSecond(request.startDate());
        long reqEnd = toEpochSecond(request.endDate());

        if (reqStart >= reqEnd) {
            throw new BookingException(BOOKING_START_DATE_NOT_VALID_TEMPLATE);
        }

        boolean hasOverlap = bookingRepository.existsOverlappingBooking(request.roomId(), reqStart, reqEnd);

        if (hasOverlap) {
            throw new BookingException(ROOM_IS_ALREADY_BOOKED_FOR_THESE_DATES_TEMPLATE);
        }

        CustomUserDetails userDetails = (CustomUserDetails) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication())
                .getPrincipal();

        if (userDetails == null) {
            throw new EntityNotFoundException(USER_NOT_FOUND_TEMPLATE.formatted("id", "some"));
        }
        User user = userService.getReferenceById(getCurrentUserId());

        Booking booking = getBooking(request, user, room, reqStart, reqEnd);
        Booking savedBooking = bookingRepository.save(booking);

        kafkaStatsPublisher.publishBookingEvent(new RoomBookedEvent(
                getCurrentUserId(),
                toLocalDate(booking.getStartDate()),
                toLocalDate(booking.getEndDate())));

        return bookingMapper.toResponse(savedBooking);
    }

    @Transactional(readOnly = true)
    public BookingResponse getById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException(BOOKING_NOT_FOUND_TEMPLATE.formatted(bookingId)));
        return bookingMapper.toResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings() {
        Long userId = getCurrentUserId();
        List<Booking> bookings = bookingRepository.findByUserId(userId);

        log.debug("Found {} bookings for userId={}", bookings.size(), userId);

        return bookings.stream()
                .map(bookingMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getAllBookings(Pageable pageable) {
        Page<Booking> bookingsPage = bookingRepository.findAll(pageable);
        return bookingsPage.map(bookingMapper::toResponse);
    }

    @Transactional
    public void cancel(Long bookingId) {
        Long currentUserId = getCurrentUserId();

        Booking booking = bookingRepository.findByIdAndUserId(bookingId, currentUserId)
                .orElseThrow(() -> {
                    if (!bookingRepository.existsById(bookingId)) {
                        return new EntityNotFoundException(BOOKING_NOT_FOUND_TEMPLATE.formatted(bookingId)
                        );
                    }
                    return new AccessDeniedException(YOURSELF_CANCEL_TEMPLATE);
                });

        bookingRepository.delete(booking);

        log.info("Booking cancelled: bookingId={}, userId={}", bookingId, currentUserId);
    }

    private Booking getBooking(BookingCreateRequest request,
                               User user,
                               Room room,
                               long reqStart,
                               long reqEnd) {
        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setUser(user);
        booking.setStartDate(reqStart);
        booking.setEndDate(reqEnd);
        booking.setGuestCount(request.guestCount());
        return booking;
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new AccessDeniedException("User is not authenticated");
        }
        return userDetails.getId();
    }

}
