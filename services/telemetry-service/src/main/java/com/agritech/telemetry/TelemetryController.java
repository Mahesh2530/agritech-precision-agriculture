package com.agritech.telemetry;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TelemetryController {

    private final List<Map<String, Object>> readings = new ArrayList<>();

    @PostMapping("/telemetry/ingest")
    public ResponseEntity<Map<String, Object>> ingest(@RequestBody IngestRequest request) {
        Map<String, Object> reading = Map.of(
                "deviceCode", request.deviceCode(),
                "value", request.value(),
                "status", "stored",
                "metric", "SOIL_MOISTURE"
        );
        readings.add(reading);
        return ResponseEntity.ok(reading);
    }

    @GetMapping("/fields/{fieldId}/latest")
    public List<Map<String, Object>> latest(@PathVariable Long fieldId) {
        return readings;
    }

    public record IngestRequest(String deviceCode, Double value) {}
}
