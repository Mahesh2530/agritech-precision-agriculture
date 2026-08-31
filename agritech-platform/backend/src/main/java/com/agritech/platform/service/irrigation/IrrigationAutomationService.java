package com.agritech.platform.service.irrigation;

import com.agritech.platform.domain.*;
import com.agritech.platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IrrigationAutomationService {

    private final SensorRepository sensorRepository;
    private final SensorReadingRepository sensorReadingRepository;
    private final IrrigationDeviceRepository irrigationDeviceRepository;
    private final IrrigationEventRepository irrigationEventRepository;
    private final IrrigationDecisionStrategy irrigationDecisionStrategy;
    private final SimpMessagingTemplate messagingTemplate;

    /** Called after every new soil-moisture reading, and by the scheduled sweep. */
    @Transactional
    public void evaluateField(Field field) {
        List<Sensor> moistureSensors = sensorRepository.findByFieldId(field.getId()).stream()
                .filter(s -> s.getMetricType() == MetricType.SOIL_MOISTURE)
                .toList();
        if (moistureSensors.isEmpty()) return;

        List<IrrigationDevice> devices = irrigationDeviceRepository.findByFieldId(field.getId());
        if (devices.isEmpty()) return;

        Sensor primary = moistureSensors.get(0);
        List<SensorReading> recent = sensorReadingRepository.findRecentBySensor(primary.getId(), 10);
        recent = recent.stream().sorted((a, b) -> a.getRecordedAt().compareTo(b.getRecordedAt())).toList();

        IrrigationDecision decision = irrigationDecisionStrategy.decide(field, recent);

        IrrigationDevice device = devices.get(0);
        if (decision.shouldIrrigate() && !device.isActive()) {
            startIrrigation(device, decision.durationMinutes(), decision.source());
        }
    }

    @Transactional
    public IrrigationEvent startIrrigation(IrrigationDevice device, int durationMinutes, TriggerSource source) {
        device.setActive(true);
        irrigationDeviceRepository.save(device);

        IrrigationEvent event = irrigationEventRepository.save(IrrigationEvent.builder()
                .device(device)
                .startedAt(Instant.now())
                .triggeredBy(source)
                .stillRunning(true)
                .build());

        messagingTemplate.convertAndSend("/topic/irrigation/" + device.getField().getId(), event);
        return event;
    }

    @Transactional
    public void stopIrrigation(IrrigationDevice device) {
        device.setActive(false);
        irrigationDeviceRepository.save(device);

        irrigationEventRepository.findByDeviceIdOrderByStartedAtDesc(device.getId()).stream()
                .filter(IrrigationEvent::isStillRunning)
                .findFirst()
                .ifPresent(event -> {
                    event.setStillRunning(false);
                    event.setEndedAt(Instant.now());
                    long minutesRun = java.time.Duration.between(event.getStartedAt(), event.getEndedAt()).toMinutes();
                    double liters = Math.max(minutesRun, 1) * device.getFlowRateLitersPerMinute();
                    event.setLitersUsed(liters);
                    irrigationEventRepository.save(event);
                    messagingTemplate.convertAndSend("/topic/irrigation/" + device.getField().getId(), event);
                });
    }

    @Transactional
    public IrrigationEvent manualCommand(IrrigationDevice device, boolean activate, Integer durationMinutes) {
        if (activate) {
            return startIrrigation(device, durationMinutes != null ? durationMinutes : 15, TriggerSource.MANUAL);
        } else {
            stopIrrigation(device);
            return null;
        }
    }
}
