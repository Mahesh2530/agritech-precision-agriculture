package com.agritech.platform.scheduler;

import com.agritech.platform.domain.Sensor;
import com.agritech.platform.repository.SensorRepository;
import com.agritech.platform.service.alert.AlertEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OfflineSensorScheduler {

    private final SensorRepository sensorRepository;
    private final AlertEvaluationService alertEvaluationService;

    @Scheduled(fixedRateString = "${app.scheduler.offline-sweep-ms:300000}")
    @Transactional
    public void sweep() {
        Instant cutoff = Instant.now().minus(15, ChronoUnit.MINUTES);
        List<Sensor> silent = sensorRepository.findByOnlineTrueAndLastSeenAtBefore(cutoff);
        for (Sensor sensor : silent) {
            sensor.setOnline(false);
            sensorRepository.save(sensor);
            alertEvaluationService.raiseOfflineSensorAlert(sensor);
        }
    }
}
