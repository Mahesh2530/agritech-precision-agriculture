package com.agritech.platform.service.analytics;

import com.agritech.platform.domain.*;
import com.agritech.platform.dto.AnalyticsDtos.DailyPoint;
import com.agritech.platform.dto.AnalyticsDtos.FieldSummary;
import com.agritech.platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final FieldRepository fieldRepository;
    private final SensorRepository sensorRepository;
    private final SensorReadingRepository sensorReadingRepository;
    private final IrrigationDeviceRepository irrigationDeviceRepository;
    private final IrrigationEventRepository irrigationEventRepository;
    private final AnalyticsSnapshotRepository analyticsSnapshotRepository;

    /** Nightly rollup — called by the scheduler for every field. */
    @Transactional
    public void rollupField(Field field, LocalDate date) {
        Instant from = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant to = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<Sensor> sensors = sensorRepository.findByFieldId(field.getId());

        Double avgMoisture = average(sensors, field, MetricType.SOIL_MOISTURE, from, to);
        Double minTemp = null, maxTemp = null;
        List<SensorReading> tempReadings = sensors.stream()
                .filter(s -> s.getMetricType() == MetricType.TEMPERATURE)
                .flatMap(s -> sensorReadingRepository.findHistory(field.getId(), MetricType.TEMPERATURE, from, to).stream())
                .toList();
        if (!tempReadings.isEmpty()) {
            minTemp = tempReadings.stream().mapToDouble(SensorReading::getValue).min().orElse(0);
            maxTemp = tempReadings.stream().mapToDouble(SensorReading::getValue).max().orElse(0);
        }

        List<IrrigationDevice> devices = irrigationDeviceRepository.findByFieldId(field.getId());
        double totalLiters = 0;
        int cycles = 0;
        for (IrrigationDevice device : devices) {
            List<IrrigationEvent> events = irrigationEventRepository.findByDeviceIdOrderByStartedAtDesc(device.getId());
            for (IrrigationEvent e : events) {
                if (e.getStartedAt() != null && !e.getStartedAt().isBefore(from) && e.getStartedAt().isBefore(to)) {
                    cycles++;
                    if (e.getLitersUsed() != null) totalLiters += e.getLitersUsed();
                }
            }
        }

        Optional<AnalyticsSnapshot> existing = analyticsSnapshotRepository
                .findByFieldIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(field.getId(), date, date)
                .stream().findFirst();

        AnalyticsSnapshot snapshot = existing.orElse(AnalyticsSnapshot.builder().field(field).snapshotDate(date).build());
        snapshot.setAvgSoilMoisture(avgMoisture);
        snapshot.setMinTemperature(minTemp);
        snapshot.setMaxTemperature(maxTemp);
        snapshot.setTotalLitersUsed(totalLiters);
        snapshot.setIrrigationCycles(cycles);
        analyticsSnapshotRepository.save(snapshot);
    }

    public FieldSummary getSummary(Long fieldId, int days) {
        Field field = fieldRepository.findById(fieldId).orElseThrow();
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(days);

        List<AnalyticsSnapshot> snapshots = analyticsSnapshotRepository
                .findByFieldIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(fieldId, from, to);

        List<DailyPoint> series = snapshots.stream()
                .map(s -> new DailyPoint(s.getSnapshotDate(), s.getAvgSoilMoisture(), s.getMinTemperature(),
                        s.getMaxTemperature(), s.getTotalLitersUsed(), s.getIrrigationCycles()))
                .toList();

        double totalWater = snapshots.stream().mapToDouble(s -> s.getTotalLitersUsed() == null ? 0 : s.getTotalLitersUsed()).sum();
        double avgMoisture = snapshots.stream().filter(s -> s.getAvgSoilMoisture() != null)
                .mapToDouble(AnalyticsSnapshot::getAvgSoilMoisture).average().orElse(0);

        return new FieldSummary(field.getId(), field.getName(), series, totalWater, avgMoisture);
    }

    private Double average(List<Sensor> sensors, Field field, MetricType type, Instant from, Instant to) {
        List<SensorReading> readings = sensorReadingRepository.findHistory(field.getId(), type, from, to);
        if (readings.isEmpty()) return null;
        return readings.stream().mapToDouble(SensorReading::getValue).average().orElse(0);
    }
}
