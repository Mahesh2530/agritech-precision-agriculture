package com.agritech.platform.service.telemetry;

import com.agritech.platform.domain.Sensor;
import com.agritech.platform.domain.SensorReading;
import com.agritech.platform.dto.TelemetryDtos.IngestRequest;
import com.agritech.platform.dto.TelemetryDtos.ReadingResponse;
import com.agritech.platform.repository.SensorReadingRepository;
import com.agritech.platform.repository.SensorRepository;
import com.agritech.platform.service.alert.AlertEvaluationService;
import com.agritech.platform.service.irrigation.IrrigationAutomationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Entry point for all incoming sensor data — from HTTP gateways today,
 * from a direct MQTT subscriber in a future phase (same method signature).
 */
@Service
@RequiredArgsConstructor
public class TelemetryIngestionService {

    private final SensorRepository sensorRepository;
    private final SensorReadingRepository sensorReadingRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final AlertEvaluationService alertEvaluationService;
    private final IrrigationAutomationService irrigationAutomationService;

    @Transactional
    public ReadingResponse ingest(IngestRequest request) {
        Sensor sensor = sensorRepository.findByDeviceCode(request.deviceCode())
                .orElseThrow(() -> new IllegalArgumentException("Unknown sensor device code: " + request.deviceCode()));

        Instant recordedAt = request.recordedAt() != null ? request.recordedAt() : Instant.now();

        SensorReading reading = sensorReadingRepository.save(SensorReading.builder()
                .sensor(sensor)
                .value(request.value())
                .recordedAt(recordedAt)
                .build());

        sensor.setOnline(true);
        sensor.setLastSeenAt(recordedAt);
        sensorRepository.save(sensor);

        ReadingResponse response = new ReadingResponse(
                sensor.getId(), sensor.getName(), sensor.getMetricType(), sensor.getUnit(),
                reading.getValue(), reading.getRecordedAt());

        // Push live update to any dashboard subscribed to this field
        messagingTemplate.convertAndSend("/topic/telemetry/" + sensor.getField().getId(), response);

        // Synchronous cheap checks: alert thresholds + irrigation rule evaluation
        alertEvaluationService.evaluateReading(sensor, reading);
        irrigationAutomationService.evaluateField(sensor.getField());

        return response;
    }
}
