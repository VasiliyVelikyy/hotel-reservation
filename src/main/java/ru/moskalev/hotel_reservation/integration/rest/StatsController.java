package ru.moskalev.hotel_reservation.integration.rest;

import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.moskalev.hotel_reservation.service.CsvExportService;

import java.time.LocalDate;

import static ru.moskalev.hotel_reservation.domain.Constants.*;

@RestController
@RequestMapping(V1 + STATS)
@AllArgsConstructor
public class StatsController {
    private final CsvExportService csvExportService;

    @GetMapping(EXPORT_CSV)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<@NonNull StreamingResponseBody> exportCsv() {
        StreamingResponseBody responseBody = csvExportService::generateStatCsv;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        getAttachment())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(responseBody);
    }

    private static String getAttachment() {
        return "attachment; filename=statistics_" + LocalDate.now() + ".csv";
    }
}
