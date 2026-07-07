package ru.moskalev.hotel_reservation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static ru.moskalev.hotel_reservation.Constants.*;
import static ru.moskalev.hotel_reservation.enumeration.UserRole.ADMIN;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui",
                                "/api-docs/**",
                                "/api-docs",
                                "/favicon.ico",
                                "/error"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, V1 + USER).permitAll()

                        .requestMatchers(HttpMethod.POST, V1 + HOTEL).hasRole(ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, V1 + HOTEL + OTHER_PATH).hasRole(ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, V1 + HOTEL + OTHER_PATH).hasRole(ADMIN.name())

                        .requestMatchers(HttpMethod.POST, V1 + ROOM + OTHER_PATH).hasRole(ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, V1 + ROOM + OTHER_PATH).hasRole(ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, V1 + ROOM + OTHER_PATH).hasRole(ADMIN.name())

                        //  Получение списка всех броней — только ADMIN
                        // (Предполагаем, что GET /bookings - это список всех, а /bookings/my - свои)
                        .requestMatchers(HttpMethod.GET, V1 + BOOKING).hasRole(ADMIN.name())

                        .anyRequest().authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(Customizer.withDefaults())
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}