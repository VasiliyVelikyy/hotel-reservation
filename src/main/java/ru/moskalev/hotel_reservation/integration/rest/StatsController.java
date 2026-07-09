package ru.moskalev.hotel_reservation.integration.rest;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.moskalev.hotel_reservation.service.CsvExportService;

import java.time.LocalDate;

import static ru.moskalev.hotel_reservation.Constants.*;

@RestController
@RequestMapping(V1 + STATS)
@AllArgsConstructor
public class StatsController {
    private final CsvExportService csvExportService;

    @GetMapping(EXPORT_CSV)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadStatsCsv() {
        byte[] csvData = csvExportService.generateStatCsv();
        HttpHeaders headers = getHeaders();

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
    }

    private static HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "statistics_" + LocalDate.now() + ".csv");
        return headers;
    }
}
