package ru.moskalev.hotel_reservation.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.moskalev.hotel_reservation.annotation.Masked;
import ru.moskalev.hotel_reservation.dto.user.UserResponse;
import ru.moskalev.hotel_reservation.enumeration.MaskType;
import ru.moskalev.hotel_reservation.enumeration.UserRole;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MaskSerializer - тестирование маскирования персональных данных")
class MaskSerializerTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    record LoginDto(@Masked(MaskType.LOGIN) String login) {
    }

    record EmailDto(@Masked(MaskType.EMAIL) String email) {
    }

    record NoMaskDto(String value) {
    }

    record MixedDto(
            @Masked(MaskType.LOGIN) String login,
            @Masked(MaskType.EMAIL) String email,
            String plainText
    ) {
    }

    @ParameterizedTest(name = "Логин '{0}' должен маскироваться как '{1}'")
    @CsvSource({
            "testLogin, te*****in",
            "admin, ad***in",
            "bob, b*b",
            "alex, a***x",
            "jo, j***",
            "a, a***",
            "superlongusername, su*************me"
    })
    @DisplayName("Должен корректно маскировать логины разной длины")
    void shouldMaskLoginWithDifferentLengths(String input, String expected) throws Exception {
        // given
        LoginDto dto = new LoginDto(input);

        // when
        String json = objectMapper.writeValueAsString(dto);

        // then
        assertThat(json).contains("\"login\":\"" + expected + "\"");
    }

    @Test
    @DisplayName("Должен обрабатывать null значение логина")
    void shouldHandleNullLogin() throws Exception {
        // given
        LoginDto dto = new LoginDto(null);

        // when
        String json = objectMapper.writeValueAsString(dto);

        // then
        assertThat(json).contains("\"login\":null");
    }

    @Test
    @DisplayName("Должен обрабатывать пустую строку логина")
    void shouldHandleEmptyLogin() throws Exception {
        // given
        LoginDto dto = new LoginDto("");

        // when
        String json = objectMapper.writeValueAsString(dto);

        // then
        assertThat(json).contains("\"login\":\"\"");
    }

    @Test
    @DisplayName("Должен обрабатывать строку из пробелов")
    void shouldHandleBlankLogin() throws Exception {
        // given
        LoginDto dto = new LoginDto("   ");

        // when
        String json = objectMapper.writeValueAsString(dto);

        // then
        assertThat(json).contains("\"login\":\"   \"");
    }

    @Test
    @DisplayName("Должен обрабатывать специальные символы в логине")
    void shouldHandleSpecialCharacters() throws Exception {
        // given
        LoginDto dto = new LoginDto("user_name-123");

        // when
        String json = objectMapper.writeValueAsString(dto);

        // then
        assertThat(json).contains("\"login\":\"us*******23\"");
    }

    @ParameterizedTest(name = "Email '{0}' должен маскироваться как '{1}'")
    @CsvSource({
            "testuser@mail.dev, te******@mail.dev",
            "anna@gmail.com, an**@gmail.com",
            "jo@ya.ru, j***@ya.ru",
            "a@domain.com, a***@domain.com",
            "ab@test.org, a***@test.org",
            "verylongusername@subdomain.example.com, ve**************@subdomain.example.com"
    })
    @DisplayName("Должен маскировать только локальную часть email")
    void shouldMaskEmailLocalPartOnly(String input, String expected) throws Exception {
        // given
        EmailDto dto = new EmailDto(input);

        // when
        String json = objectMapper.writeValueAsString(dto);

        // then
        assertThat(json).contains("\"email\":\"" + expected + "\"");
    }

    @Test
    @DisplayName("Должен обрабатывать email без символа @")
    void shouldHandleEmailWithoutAtSymbol() throws Exception {
        // given
        EmailDto dto = new EmailDto("invalidemail");

        // when
        String json = objectMapper.writeValueAsString(dto);

        // then
        // Fallback на маскирование как логин
        assertThat(json).contains("\"email\":\"in******il\"");
    }

    @Test
    @DisplayName("Должен обрабатывать null email")
    void shouldHandleNullEmail() throws Exception {
        // given
        EmailDto dto = new EmailDto(null);

        // when
        String json = objectMapper.writeValueAsString(dto);

        // then
        assertThat(json).contains("\"email\":null");
    }

    @Test
    @DisplayName("Должен сохранять доменную часть без изменений")
    void shouldPreserveDomainPart() throws Exception {
        // given
        EmailDto dto = new EmailDto("testuser@company.com");

        // when
        String json = objectMapper.writeValueAsString(dto);

        // then
        assertThat(json).contains("@company.com");
        assertThat(json).doesNotContain("testuser@");
    }

    @Test
    @DisplayName("Должен обрабатывать email с поддоменами")
    void shouldHandleEmailWithSubdomains() throws Exception {
        // given
        EmailDto dto = new EmailDto("user@mail.subdomain.example.com");

        // when
        String json = objectMapper.writeValueAsString(dto);

        // then
        assertThat(json).contains("\"email\":\"us**@mail.subdomain.example.com\"");
    }

    @Test
    @DisplayName("Должен применять разные типы маскировки в одном объекте")
    void shouldApplyDifferentMaskTypes() throws Exception {
        // given
        MixedDto dto = new MixedDto("testLogin", "user@example.com", "plain");

        // when
        String json = objectMapper.writeValueAsString(dto);

        // then
        assertThat(json)
                .contains("\"login\":\"te*****in\"")
                .contains("\"email\":\"us**@example.com\"")
                .contains("\"plainText\":\"plain\"");
    }

    @Test
    @DisplayName("Должен работать без аннотации @Masked")
    void shouldWorkWithoutMaskAnnotation() throws Exception {
        // given
        NoMaskDto dto = new NoMaskDto("sensitiveData");

        // when
        String json = objectMapper.writeValueAsString(dto);

        // then
        assertThat(json).contains("\"value\":\"sensitiveData\"");
    }

    @Test
    @DisplayName("Должен корректно сериализовать полный UserResponse")
    void shouldSerializeFullUserResponse() throws Exception {
        // given
        UserResponse response = new UserResponse(
                1L,
                "testLogin",
                "testuser@mail.dev",
                UserRole.CLIENT
        );

        // when
        String json = objectMapper.writeValueAsString(response);

        // then
        assertThat(json)
                .contains("\"id\":1")
                .contains("\"login\":\"te*****in\"")
                .contains("\"email\":\"te******@mail.dev\"")
                .contains("\"role\":\"CLIENT\"");
    }
}