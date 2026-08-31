package com.agritech.platform.controller;

import com.agritech.platform.domain.MetricType;
import com.agritech.platform.domain.Sensor;
import com.agritech.platform.dto.TelemetryDtos.*;
import com.agritech.platform.repository.SensorReadingRepository;
import com.agritech.platform.repository.SensorRepository;
import com.agritech.platform.service.telemetry.TelemetryIngestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class TelemetryController {

    private final TelemetryIngestionService ingestionService;
    private final SensorReadingRepository sensorReadingRepository;
    private final SensorRepository sensorRepository;

    /** Public ingestion endpoint for sensor gateways (secured by device API key in production — see README). */
    @PostMapping("/api/telemetry/ingest")
    public ResponseEntity<ReadingResponse> ingest(@Valid @RequestBody IngestRequest request) {
        return ResponseEntity.ok(ingestionService.ingest(request));
    }

    @GetMapping("/api/fields/{fieldId}/readings")
    public List<ReadingResponse> history(@PathVariable Long fieldId,
                                          @RequestParam MetricType metric,
                                          @RequestParam(required = false) Instant from,
                                          @RequestParam(required = false) Instant to) {
        Instant start = from != null ? from : Instant.now().minusSeconds(24 * 3600);
        Instant end = to != null ? to : Instant.now();
        return sensorReadingRepository.findHistory(fieldId, metric, start, end).stream()
                .map(r -> new ReadingResponse(r.getSensor().getId(), r.getSensor().getName(),
                        r.getSensor().getMetricType(), r.getSensor().getUnit(), r.getValue(), r.getRecordedAt()))
                .toList();
    }

    @GetMapping("/api/fields/{fieldId}/latest")
    public List<LatestSnapshot> latest(@PathVariable Long fieldId) {
        List<Sensor> sensors = sensorRepository.findByFieldId(fieldId);
        return sensors.stream()
                .flatMap(sensor -> sensorReadingRepository.findRecentBySensor(sensor.getId(), 1).stream())
                .map(r -> new LatestSnapshot(
                        r.getSensor().getMetricType(), r.getValue(), r.getSensor().getUnit(), r.getRecordedAt()))
                .toList();
    }
}
