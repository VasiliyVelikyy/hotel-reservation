package ru.moskalev.hotel_reservation.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import ru.moskalev.hotel_reservation.annotation.Masked;
import ru.moskalev.hotel_reservation.enumeration.MaskType;

import java.io.IOException;

public class MaskSerializer extends StdSerializer<String> implements ContextualSerializer {

    private final MaskType maskType;

    // Jackson требует конструктор без аргументов
    public MaskSerializer() {
        super(String.class);
        this.maskType = MaskType.LOGIN;
    }

    public MaskSerializer(MaskType maskType) {
        super(String.class);
        this.maskType = maskType;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null || value.isBlank()) {
            gen.writeString(value);
            return;
        }

        String masked = switch (maskType) {
            case LOGIN -> maskLogin(value);
            case EMAIL -> maskEmail(value);
        };

        gen.writeString(masked);
    }

    /**
     * LOGIN: "testLogin" → "te*****in"
     * Короткие (<=4): "alex" → "a***"
     */
    private String maskLogin(String login) {
        if (login.length() <= 2) {
            return login.charAt(0) + "***";
        }
        if (login.length() <= 4) {
            return login.charAt(0) + "***" + login.charAt(login.length() - 1);
        }
        int visibleStart = 2;
        int visibleEnd = 2;
        int starsCount = login.length() - visibleStart - visibleEnd;
        return login.substring(0, visibleStart)
                + "*".repeat(starsCount)
                + login.substring(login.length() - visibleEnd);
    }

    /**
     * EMAIL: "testuser@mail.dev" → "te******@mail.dev"
     * Маскируется только локальная часть (до @)
     */
    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return maskLogin(email); // fallback, если нет @
        }

        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex); // включает @

        if (localPart.length() <= 2) {
            return localPart.charAt(0) + "***" + domainPart;
        }

        int visibleStart = 2;
        int starsCount = localPart.length() - visibleStart;
        return localPart.substring(0, visibleStart)
                + "*".repeat(starsCount)
                + domainPart;
    }

    /**
     * ContextualSerializer позволяет прочитать аннотацию @Masked(value = ...)
     * и создать сериализатор с нужным типом маскировки.
     */
    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
            throws JsonMappingException {

        Masked annotation = null;

        if (property != null) {
            annotation = property.getAnnotation(Masked.class);
            if (annotation == null) {
                annotation = property.getContextAnnotation(Masked.class);
            }
        }

        MaskType type = (annotation != null) ? annotation.value() : MaskType.LOGIN;
        return new MaskSerializer(type);
    }
}