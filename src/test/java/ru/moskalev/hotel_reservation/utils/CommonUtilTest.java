package ru.moskalev.hotel_reservation.utils;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import ru.moskalev.hotel_reservation.exception.PaginatedException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CommonUtil")
class CommonUtilTest {

    @Test
    @DisplayName("getSort: должен создать Sort с направлением ASC при direction = 'asc'")
    void getSort_shouldCreateAscendingSort_whenDirectionIsAsc() {
        // given
        String sortBy = "name";
        String direction = "asc";

        // when
        Sort result = CommonUtil.getSort(sortBy, direction);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSorted()).isTrue();
        assertThat(result.getOrderFor("name")).isNotNull();
        assertThat(Objects.requireNonNull(result.getOrderFor("name"))
                .getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @DisplayName("getSort: должен создать Sort с направлением ASC при direction = 'ASC' (верхний регистр)")
    void getSort_shouldCreateAscendingSort_whenDirectionIsUpperCaseAsc() {
        // given
        String sortBy = "id";
        String direction = "ASC";

        // when
        Sort result = CommonUtil.getSort(sortBy, direction);

        // then
        assertThat(result).isNotNull();
        assertThat(Objects.requireNonNull(result.getOrderFor("id")).getDirection())
                .isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @DisplayName("getSort: должен создать Sort с направлением DESC при direction = 'desc'")
    void getSort_shouldCreateDescendingSort_whenDirectionIsDesc() {
        // given
        String sortBy = "price";
        String direction = "desc";

        // when
        Sort result = CommonUtil.getSort(sortBy, direction);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isSorted()).isTrue();
        assertThat(result.getOrderFor("price")).isNotNull();
        assertThat(Objects.requireNonNull(result.getOrderFor("price"))
                .getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("getSort: должен создать Sort с направлением DESC при direction = 'DESC' (верхний регистр)")
    void getSort_shouldCreateDescendingSort_whenDirectionIsUpperCaseDesc() {
        // given
        String sortBy = "createdAt";
        String direction = "DESC";

        // when
        Sort result = CommonUtil.getSort(sortBy, direction);

        // then
        assertThat(result).isNotNull();
        assertThat(Objects.requireNonNull(result.getOrderFor("createdAt"))
                .getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("getSort: должен выбросить PaginatedException при невалидном direction")
    void getSort_shouldThrowException_whenDirectionIsInvalid() {
        // given
        String sortBy = "name";
        String direction = "invalid";

        // when & then
        assertThatThrownBy(() -> CommonUtil.getSort(sortBy, direction))
                .isInstanceOf(PaginatedException.class);
    }

    @Test
    @DisplayName("getSort: должен выбросить PaginatedException при direction = 'random'")
    void getSort_shouldThrowException_whenDirectionIsRandom() {
        // given
        String sortBy = "id";
        String direction = "random";

        // when & then
        assertThatThrownBy(() -> CommonUtil.getSort(sortBy, direction))
                .isInstanceOf(PaginatedException.class);
    }

    @Test
    @DisplayName("getSort: должен работать с разными именами полей для сортировки")
    void getSort_shouldWorkWithDifferentFieldNames() {
        // given
        String[] fieldNames = {"id", "name", "price", "createdAt", "hotel_id"};

        // when & then
        for (String fieldName : fieldNames) {
            Sort result = CommonUtil.getSort(fieldName, "asc");
            assertThat(result.getOrderFor(fieldName)).isNotNull();
            assertThat(Objects.requireNonNull(result.getOrderFor(fieldName))
                    .getDirection())
                    .isEqualTo(Sort.Direction.ASC);
        }
    }

    @Test
    @DisplayName("updateIfNotNull: должен вызвать setter, если newValue не null")
    void updateIfNotNull_shouldCallSetter_whenNewValueIsNotNull() {
        // given
        String newValue = "new value";
        AtomicReference<String> target = new AtomicReference<>("old value");
        Consumer<String> setter = target::set;

        // when
        CommonUtil.updateIfNotNull(newValue, setter);

        // then
        assertThat(target.get()).isEqualTo("new value");
    }

    @Test
    @DisplayName("updateIfNotNull: НЕ должен вызвать setter, если newValue равен null")
    void updateIfNotNull_shouldNotCallSetter_whenNewValueIsNull() {
        // given
        String newValue = null;
        AtomicReference<String> target = new AtomicReference<>("old value");
        Consumer<String> setter = target::set;

        // when
        CommonUtil.updateIfNotNull(newValue, setter);

        // then
        assertThat(target.get()).isEqualTo("old value");
    }

    @Test
    @DisplayName("updateIfNotNull: должен работать с типом String")
    void updateIfNotNull_shouldWorkWithStringType() {
        // given
        StringBuilder sb = new StringBuilder();
        Consumer<String> setter = sb::append;

        // when
        CommonUtil.updateIfNotNull("test", setter);

        var actual = sb.toString();
        // then
        assertThat(actual).isEqualTo("test");
    }

    @Test
    @DisplayName("updateIfNotNull: должен работать с типом Integer")
    void updateIfNotNull_shouldWorkWithIntegerType() {
        // given
        AtomicReference<Integer> target = new AtomicReference<>(0);
        Consumer<Integer> setter = target::set;

        // when
        CommonUtil.updateIfNotNull(42, setter);

        // then
        assertThat(target.get()).isEqualTo(42);
    }

    @Test
    @DisplayName("updateIfNotNull: должен работать с типом Object")
    void updateIfNotNull_shouldWorkWithObjectType() {
        // given
        AtomicReference<Object> target = new AtomicReference<>();
        Consumer<Object> setter = target::set;
        Object testObject = new Object();

        // when
        CommonUtil.updateIfNotNull(testObject, setter);

        // then
        assertThat(target.get()).isSameAs(testObject);
    }

    @Test
    @DisplayName("updateIfNotNull: должен корректно обрабатывать пустую строку (не null)")
    void updateIfNotNull_shouldCallSetter_whenNewValueIsEmptyString() {
        // given
        String newValue = "";
        AtomicReference<String> target = new AtomicReference<>("old");
        Consumer<String> setter = target::set;

        // when
        CommonUtil.updateIfNotNull(newValue, setter);

        // then
        assertThat(target.get()).isEmpty();
    }

    @Test
    @DisplayName("updateIfNotNull: должен корректно обрабатывать нулевое число (не null)")
    void updateIfNotNull_shouldCallSetter_whenNewValueIsZero() {
        // given
        Integer newValue = 0;
        AtomicReference<Integer> target = new AtomicReference<>(100);
        Consumer<Integer> setter = target::set;

        // when
        CommonUtil.updateIfNotNull(newValue, setter);

        // then
        assertThat(target.get()).isZero();
    }

    @Test
    @DisplayName("toEpochSecond: должен конвертировать LocalDate в epoch seconds")
    void toEpochSecond_shouldConvertLocalDateToEpochSeconds() {
        // given
        LocalDate date = LocalDate.of(2026, 7, 9);

        // when
        long result = CommonUtil.toEpochSecond(date);

        // then
        assertThat(result).isGreaterThan(0);
    }

    @Test
    @DisplayName("toEpochSecond: должен конвертировать дату в начало дня (00:00:00)")
    void toEpochSecond_shouldConvertToStartOfDay() {
        // given
        LocalDate date = LocalDate.of(2026, 7, 9);
        long expectedSeconds = date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

        // when
        long result = CommonUtil.toEpochSecond(date);

        // then
        assertThat(result).isEqualTo(expectedSeconds);
    }

    @Test
    @DisplayName("toEpochSecond: должен корректно обрабатывать известную дату (2020-01-01)")
    void toEpochSecond_shouldHandleKnownDate() {
        // given
        LocalDate date = LocalDate.of(2020, 1, 1);

        // when
        long result = CommonUtil.toEpochSecond(date);

        // then
        assertThat(result).isGreaterThan(0);
        long expectedSeconds = date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        assertThat(result).isEqualTo(expectedSeconds);
    }

    @Test
    @DisplayName("toEpochSecond: должен возвращать разные значения для разных дат")
    void toEpochSecond_shouldReturnDifferentValuesForDifferentDates() {
        // given
        LocalDate date1 = LocalDate.of(2026, 1, 1);
        LocalDate date2 = LocalDate.of(2026, 1, 2);

        // when
        long result1 = CommonUtil.toEpochSecond(date1);
        long result2 = CommonUtil.toEpochSecond(date2);

        // then
        assertThat(result1).isNotEqualTo(result2);
        assertThat(result2).isGreaterThan(result1);
    }

    @Test
    @DisplayName("toEpochSecond: должен корректно обрабатывать високосный год (2024-02-29)")
    void toEpochSecond_shouldHandleLeapYear() {
        // given
        LocalDate date = LocalDate.of(2024, 2, 29);

        // when
        long result = CommonUtil.toEpochSecond(date);

        // then
        assertThat(result).isGreaterThan(0);
        long expectedSeconds = date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        assertThat(result).isEqualTo(expectedSeconds);
    }

    @Test
    @DisplayName("toEpochSecond: должен использовать системную временную зону")
    void toEpochSecond_shouldUseSystemTimeZone() {
        // given
        LocalDate date = LocalDate.of(2026, 7, 9);
        ZoneId systemZone = ZoneId.systemDefault();

        // when
        long result = CommonUtil.toEpochSecond(date);

        // then
        long expectedSeconds = date.atStartOfDay(systemZone).toEpochSecond();
        assertThat(result).isEqualTo(expectedSeconds);
    }

    @Test
    @DisplayName("toEpochSecond: должен возвращать значение в секундах (не миллисекундах)")
    void toEpochSecond_shouldReturnSecondsNotMilliseconds() {
        // given
        LocalDate date = LocalDate.of(2026, 7, 9);

        // when
        long result = CommonUtil.toEpochSecond(date);

        // then
        // Типичное значение для 2026 года ~ 1.7 миллиарда секунд
        assertThat(result).isBetween(1_700_000_000L, 1_800_000_000L);
    }
}