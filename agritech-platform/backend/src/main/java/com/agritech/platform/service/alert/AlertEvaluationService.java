package com.agritech.platform.service.alert;

import com.agritech.platform.domain.*;
import com.agritech.platform.repository.AlertRepository;
import com.agritech.platform.repository.AlertRuleRepository;
import com.agritech.platform.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertEvaluationService {

    private final AlertRuleRepository alertRuleRepository;
    private final AlertRepository alertRepository;
    private final SensorRepository sensorRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /** Called synchronously on every ingested reading — cheap threshold check. */
    @Transactional
    public void evaluateReading(Sensor sensor, SensorReading reading) {
        Field field = sensor.getField();
        List<AlertRule> rules = alertRuleRepository.findByFieldIdAndEnabledTrue(field.getId());

        for (AlertRule rule : rules) {
            if (rule.getMetricType() != sensor.getMetricType()) continue;
            boolean triggered = "LT".equals(rule.getOperator())
                    ? reading.getValue() < rule.getThreshold()
                    : reading.getValue() > rule.getThreshold();
            if (triggered) {
                raise(field, mapAlertType(sensor.getMetricType(), rule.getOperator()), rule.getSeverity(),
                        String.format("%s reading %.1f %s crossed threshold %.1f on %s",
                                sensor.getMetricType(), reading.getValue(),
                                sensor.getUnit() == null ? "" : sensor.getUnit(),
                                rule.getThreshold(), field.getName()));
            }
        }

        // Built-in default: soil moisture below the field's own irrigation threshold is at least an INFO alert
        if (sensor.getMetricType() == MetricType.SOIL_MOISTURE && field.getMoistureThreshold() != null
                && reading.getValue() < field.getMoistureThreshold()) {
            raise(field, AlertType.LOW_MOISTURE, AlertSeverity.WARNING,
                    String.format("Soil moisture %.1f%% is below the %s irrigation threshold (%.1f%%)",
                            reading.getValue(), field.getName(), field.getMoistureThreshold()));
        }
    }

    /** Called by the scheduled sweep every few minutes. */
    @Transactional
    public void raiseOfflineSensorAlert(Sensor sensor) {
        raise(sensor.getField(), AlertType.SENSOR_OFFLINE, AlertSeverity.CRITICAL,
                String.format("Sensor '%s' (%s) has not reported in over 15 minutes", sensor.getName(), sensor.getDeviceCode()));
    }

    private void raise(Field field, AlertType type, AlertSeverity severity, String message) {
        Alert alert = alertRepository.save(Alert.builder()
                .field(field).type(type).severity(severity).message(message)
                .acknowledged(false).createdAt(Instant.now()).build());
        messagingTemplate.convertAndSend("/topic/alerts/" + field.getFarm().getId(), alert);
    }

    private AlertType mapAlertType(MetricType metricType, String operator) {
        if (metricType == MetricType.SOIL_MOISTURE) return AlertType.LOW_MOISTURE;
        if (metricType == MetricType.TEMPERATURE) return "GT".equals(operator) ? AlertType.HIGH_TEMPERATURE : AlertType.FROST_RISK;
        return AlertType.DEVICE_FAULT;
    }
}
