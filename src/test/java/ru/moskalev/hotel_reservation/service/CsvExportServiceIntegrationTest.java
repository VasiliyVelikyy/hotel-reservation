package ru.moskalev.hotel_reservation.service;

import com.opencsv.CSVReader;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.moskalev.hotel_reservation.domain.StatEventDocument;
import ru.moskalev.hotel_reservation.repo.StatEventRepository;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.fail;
import static ru.moskalev.hotel_reservation.utils.TestUtils.containsRow;

@SpringBootTest
class CsvExportServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CsvExportService csvExportService;

    @Autowired
    private StatEventRepository statEventRepository;

    @BeforeEach
    void setUp() {
        statEventRepository.deleteAll();
    }

    @Test
    void shouldGenerateCorrectCsvWithEvents() {
        // given
        LocalDateTime now = LocalDateTime.of(2026, 7, 12, 10, 0, 0);

        StatEventDocument userEvent = StatEventDocument.builder()
                .eventType("USER_REGISTERED")
                .userId(100L)
                .createdAt(now)
                .build();

        StatEventDocument bookingEvent = StatEventDocument.builder()
                .eventType("ROOM_BOOKED")
                .userId(200L)
                .checkInDate(LocalDate.of(2026, 7, 15))
                .checkOutDate(LocalDate.of(2026, 7, 20))
                .createdAt(now.plusHours(1))
                .build();

        statEventRepository.saveAll(List.of(userEvent, bookingEvent));

        // when
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        csvExportService.generateStatCsv(stream);
        String csvContent = stream.toString(StandardCharsets.UTF_8);

        // then
        try (CSVReader reader = new CSVReader(new StringReader(csvContent))) {
            List<String[]> rows = reader.readAll();

            Assertions.assertThat(rows).hasSize(3);

            Assertions.assertThat(rows.getFirst()).containsExactly(
                    CsvExportService.EVENT_TYPE_COLUMN,
                    CsvExportService.USER_ID_COLUMN,
                    CsvExportService.CHECK_IN_COLUMN,
                    CsvExportService.CHECK_OUT_COLUMN,
                    CsvExportService.CREATED_AT_COLUMN
            );

            boolean hasUserEvent = containsRow(rows, "USER_REGISTERED", "100", "", "");
            boolean hasBookingEvent = containsRow(rows, "ROOM_BOOKED", "200", "2026-07-15", "2026-07-20");

            Assertions.assertThat(hasUserEvent)
                    .as("Ожидается наличие события USER_REGISTERED с userId=100")
                    .isTrue();

            Assertions.assertThat(hasBookingEvent)
                    .as("Ожидается наличие события ROOM_BOOKED с датами 2026-07-15 и 2026-07-20")
                    .isTrue();

        } catch (Exception e) {
            fail("Не удалось обработать сгенерированный CSV", e);
        }
    }

    @Test
    void shouldGenerateOnlyHeadersWhenDatabaseIsEmpty() {
        // when
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        csvExportService.generateStatCsv(stream);
        String csvContent = stream.toString(StandardCharsets.UTF_8);

        // then
        try (CSVReader reader = new CSVReader(new StringReader(csvContent))) {
            List<String[]> rows = reader.readAll();

            Assertions.assertThat(rows).hasSize(1);
            Assertions.assertThat(rows.getFirst()).containsExactly(
                    CsvExportService.EVENT_TYPE_COLUMN,
                    CsvExportService.USER_ID_COLUMN,
                    CsvExportService.CHECK_IN_COLUMN,
                    CsvExportService.CHECK_OUT_COLUMN,
                    CsvExportService.CREATED_AT_COLUMN
            );
        } catch (Exception e) {
            fail("Не удалось обработать сгенерированный CSV", e);
        }
    }

    /**
     * Дополнительный тест: проверяем, что сервис корректно обрабатывает большое количество записей.
     * Это косвенно подтверждает, что используется Stream (а не List),
     * иначе мы бы упирались в ограничения памяти.
     */
    @Test
    void shouldStreamLargeAmountOfData() {
        // given - 1000 событий
        LocalDateTime baseTime = LocalDateTime.of(2026, 7, 12, 10, 0, 0);
        List<StatEventDocument> events = IntStream.rangeClosed(1, 1000)
                .mapToObj(i -> StatEventDocument.builder()
                        .eventType("USER_REGISTERED")
                        .userId((long) i)
                        .createdAt(baseTime.plusSeconds(i))
                        .build())
                .toList();

        statEventRepository.saveAll(events);

        // when
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        csvExportService.generateStatCsv(stream);
        String csvContent = stream.toString(StandardCharsets.UTF_8);

        // then
        try (CSVReader reader = new CSVReader(new StringReader(csvContent))) {
            List<String[]> rows = reader.readAll();

            Assertions.assertThat(rows).hasSize(1001);

            long distinctUserIds = rows.stream()
                    .skip(1) // пропускаем заголовок
                    .map(row -> row[1])
                    .distinct()
                    .count();

            Assertions.assertThat(distinctUserIds).isEqualTo(1000);
        } catch (Exception e) {
            fail("Не удалось обработать сгенерированный CSV", e);
        }
    }

}