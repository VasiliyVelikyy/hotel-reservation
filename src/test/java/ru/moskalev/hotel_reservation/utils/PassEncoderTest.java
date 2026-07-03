package ru.moskalev.hotel_reservation.utils;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class PassEncoderTest {
    @Test
    void shouldEncodePasswordWithArgon2() {
        PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

        String rawPassword = "mySecretPassword123";
        String encoded = encoder.encode(rawPassword);

        assertThat(encoded).startsWith("$argon2id$");
        assertThat(encoded.length()).isGreaterThan(100);

        assertThat(encoder.matches(rawPassword, encoded)).isTrue();
        assertThat(encoder.matches("wrongPassword", encoded)).isFalse();
    }
}
