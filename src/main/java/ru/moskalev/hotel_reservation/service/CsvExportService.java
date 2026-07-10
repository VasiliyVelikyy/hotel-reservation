package ru.moskalev.hotel_reservation.service;

import com.opencsv.CSVWriter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.moskalev.hotel_reservation.domain.StatEventDocument;
import ru.moskalev.hotel_reservation.exception.CsvGenerateException;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class CsvExportService {
    public static final String EVENT_TYPE_COLUMN = "Event Type";
    public static final String USER_ID_COLUMN = "User ID";
    public static final String CHECK_IN_COLUMN = "Check-In";
    public static final String CHECK_OUT_COLUMN = "Check-Out";
    public static final String CREATED_AT_COLUMN = "Created At";
    private final StatsService service;

    public byte[] generateStatCsv() {
        List<StatEventDocument> events = service.findAll();
        StringWriter sw = new StringWriter();
        try (CSVWriter writer = new CSVWriter(sw)) {
            writer.writeNext(new String[]{EVENT_TYPE_COLUMN, USER_ID_COLUMN, CHECK_IN_COLUMN, CHECK_OUT_COLUMN, CREATED_AT_COLUMN});

            for (var doc : events) {
                var checkIn = doc.getCheckInDate() != null ? doc.getCheckInDate().toString() : "";
                var checkOut = doc.getCheckOutDate() != null ? doc.getCheckOutDate().toString() : "";
                writer.writeNext(new String[]{doc.getEventType(), String.valueOf(doc.getUserId()), checkIn, checkOut, doc.getCreatedAt().toString()});
            }

        } catch (IOException e) {
            log.error(e.getMessage());
            throw new CsvGenerateException("Error generating CSV", e);
        }
        return sw.toString().getBytes(StandardCharsets.UTF_8);
    }

}
