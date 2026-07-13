package ru.moskalev.hotel_reservation.utils;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.moskalev.hotel_reservation.domain.*;
import ru.moskalev.hotel_reservation.dto.hotel.HotelCreateInput;
import ru.moskalev.hotel_reservation.enumeration.UserRole;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static ru.moskalev.hotel_reservation.TestConstants.*;

public class TestUtils {

    public static Room getRoom(String name) {
        Room room = new Room();
        room.setName(name);
        room.setDescription("Description");
        room.setNumber((short) 100);
        room.setPrice(new BigDecimal("1000.00"));
        room.setMaxCount((byte) 2);
        return room;
    }


    public static User buildUser(String login, String email, String hashPassword, UserRole role) {
        User user = new User();
        user.setLogin(login);
        user.setEmail(email);
        user.setHashPassword(hashPassword);
        user.setRole(role);
        return user;
    }

    public static User buildUser(String login) {
        return buildUser(login, login + "@test.com", "hashedPassword123", UserRole.CLIENT);
    }

    public static Hotel buildHotel(String name) {
        Hotel hotel = new Hotel();
        hotel.setName(name);
        hotel.setDescription("Description");
        hotel.setTitle("Title");
        hotel.setCity("City");
        hotel.setAddress(ADDRESS);
        hotel.setDistance(100);
        hotel.setGrade(new Grade(0D, 0D, 0));
        return hotel;
    }

    /**
     * Вспомогательный метод для установки аутентификации в тестах
     */
    public static void setAuthentication(Long userId, String username) {
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

    public static boolean containsRow(List<String[]> rows, String eventType, String userId, String checkIn, String checkOut) {
        return rows.stream().skip(1) // Пропускаем заголовок
                .anyMatch(row ->
                        row[0].equals(eventType) &&
                                row[1].equals(userId) &&
                                row[2].equals(checkIn) &&
                                row[3].equals(checkOut)
                );
    }

    public static HotelCreateInput getHotelCreateInput() {
        return new HotelCreateInput(
                HOSTEL_NAME,
                HOSTEL_DESCRIPTION,
                TITLE,
                CITY,
                ADDRESS,
                1
        );
    }
}
