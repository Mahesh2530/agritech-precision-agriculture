package com.agritech.platform.config;

import com.agritech.platform.domain.*;
import com.agritech.platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Random;

/**
 * Seeds the in-memory (dev/H2) database with a believable demo dataset so the app is never
 * empty on first run: an org/admin/farm/fields, sensors + ~24h of mock telemetry history,
 * a couple of realistic alerts, irrigation event history, and a week of analytics rollups.
 *
 * This only runs when the database is empty (see the early return below), so it never
 * overwrites real data -- safe to leave enabled in the dev profile.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final FarmRepository farmRepository;
    private final FieldRepository fieldRepository;
    private final SensorRepository sensorRepository;
    private final SensorReadingRepository sensorReadingRepository;
    private final IrrigationDeviceRepository irrigationDeviceRepository;
    private final IrrigationEventRepository irrigationEventRepository;
    private final AlertRuleRepository alertRuleRepository;
    private final AlertRepository alertRepository;
    private final AnalyticsSnapshotRepository analyticsSnapshotRepository;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random(42); // fixed seed -> reproducible demo data

    @Override
    public void run(String... args) {
        if (organizationRepository.count() > 0) return;

        Organization org = organizationRepository.save(Organization.builder().name("Green Valley Farms Co-op").build());

        userRepository.save(User.builder()
                .email("admin@agritech.dev")
                .passwordHash(passwordEncoder.encode("admin123"))
                .fullName("Ava Admin")
                .role(Role.ADMIN)
                .organization(org)
                .build());

        Farm farm = farmRepository.save(Farm.builder()
                .name("North Ridge Farm")
                .location("Guntur, Andhra Pradesh")
                .latitude(16.3067).longitude(80.4365)
                .organization(org)
                .build());

        Field field1 = fieldRepository.save(Field.builder()
                .name("Field A - Paddy")
                .areaHectares(4.2).cropType("Rice")
                .latitude(16.308).longitude(80.437)
                .moistureThreshold(35.0).defaultIrrigationMinutes(20)
                .farm(farm).build());

        Field field2 = fieldRepository.save(Field.builder()
                .name("Field B - Cotton")
                .areaHectares(6.0).cropType("Cotton")
                .latitude(16.305).longitude(80.440)
                .moistureThreshold(25.0).defaultIrrigationMinutes(15)
                .farm(farm).build());

        Sensor moistureA = sensorRepository.save(Sensor.builder().deviceCode("SM-A1").name("Soil Moisture A1")
                .metricType(MetricType.SOIL_MOISTURE).unit("%").online(true).lastSeenAt(Instant.now()).field(field1).build());
        Sensor tempA = sensorRepository.save(Sensor.builder().deviceCode("TP-A1").name("Temperature A1")
                .metricType(MetricType.TEMPERATURE).unit("°C").online(true).lastSeenAt(Instant.now()).field(field1).build());
        Sensor moistureB = sensorRepository.save(Sensor.builder().deviceCode("SM-B1").name("Soil Moisture B1")
                .metricType(MetricType.SOIL_MOISTURE).unit("%").online(true).lastSeenAt(Instant.now()).field(field2).build());
        Sensor tempB = sensorRepository.save(Sensor.builder().deviceCode("TP-B1").name("Temperature B1")
                .metricType(MetricType.TEMPERATURE).unit("°C").online(false)
                .lastSeenAt(Instant.now().minus(40, ChronoUnit.MINUTES)).field(field2).build());

        IrrigationDevice valveA = irrigationDeviceRepository.save(IrrigationDevice.builder()
                .deviceCode("VALVE-A1").name("Field A Main Valve").flowRateLitersPerMinute(22.0)
                .active(false).field(field1).build());
        IrrigationDevice valveB = irrigationDeviceRepository.save(IrrigationDevice.builder()
                .deviceCode("VALVE-B1").name("Field B Main Valve").flowRateLitersPerMinute(18.0)
                .active(true).field(field2).build());

        // --- Mock telemetry: last 24h at 30-minute intervals, gentle day/night curves + noise ---
        seedMoistureHistory(moistureA, 42.0);
        seedTemperatureHistory(tempA, 27.0);
        seedMoistureHistory(moistureB, 29.0);
        seedTemperatureHistory(tempB, 26.0);

        // --- Alert rules (used for future ingested readings, not just the seed) ---
        alertRuleRepository.save(AlertRule.builder().field(field1).metricType(MetricType.TEMPERATURE)
                .operator("GT").threshold(38.0).severity(AlertSeverity.WARNING).enabled(true).build());
        alertRuleRepository.save(AlertRule.builder().field(field2).metricType(MetricType.TEMPERATURE)
                .operator("LT").threshold(8.0).severity(AlertSeverity.CRITICAL).enabled(true).build());

        // --- A few realistic alerts already on the board ---
        alertRepository.save(Alert.builder().field(field1).type(AlertType.LOW_MOISTURE)
                .severity(AlertSeverity.WARNING)
                .message("Soil moisture 31.4% is below the Field A - Paddy irrigation threshold (35.0%)")
                .acknowledged(false).createdAt(Instant.now().minus(3, ChronoUnit.HOURS)).build());
        alertRepository.save(Alert.builder().field(field2).type(AlertType.SENSOR_OFFLINE)
                .severity(AlertSeverity.CRITICAL)
                .message("Sensor 'Temperature B1' (TP-B1) has not reported in over 15 minutes")
                .acknowledged(false).createdAt(Instant.now().minus(40, ChronoUnit.MINUTES)).build());
        alertRepository.save(Alert.builder().field(field2).type(AlertType.IRRIGATION_STARTED)
                .severity(AlertSeverity.INFO)
                .message("Field B Main Valve started automatically -- soil moisture below threshold")
                .acknowledged(true).createdAt(Instant.now().minus(1, ChronoUnit.HOURS)).build());

        // --- Irrigation history: one completed cycle on Field A, one still running on Field B ---
        Instant completedStart = Instant.now().minus(5, ChronoUnit.HOURS);
        Instant completedEnd = completedStart.plus(20, ChronoUnit.MINUTES);
        irrigationEventRepository.save(IrrigationEvent.builder()
                .device(valveA).startedAt(completedStart).endedAt(completedEnd)
                .litersUsed(20 * valveA.getFlowRateLitersPerMinute())
                .triggeredBy(TriggerSource.RULE).stillRunning(false).build());
        irrigationEventRepository.save(IrrigationEvent.builder()
                .device(valveB).startedAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .triggeredBy(TriggerSource.RULE).stillRunning(true).build());

        // --- Analytics rollups for the past 7 days so the Analytics page isn't empty on day 1 ---
        seedAnalyticsHistory(field1, 41.0, 22.0, 34.0, 3);
        seedAnalyticsHistory(field2, 28.0, 21.0, 33.0, 2);
    }

    private void seedMoistureHistory(Sensor sensor, double baseline) {
        Instant now = Instant.now();
        for (int i = 48; i >= 0; i--) { // 48 points -> 24h at 30 min steps
            Instant recordedAt = now.minus(i * 30L, ChronoUnit.MINUTES);
            double drift = -0.15 * (48 - i); // slow drying trend through the day
            double noise = (random.nextDouble() - 0.5) * 2.5;
            double value = Math.max(5, baseline + drift + noise);
            sensorReadingRepository.save(SensorReading.builder()
                    .sensor(sensor).value(round1(value)).recordedAt(recordedAt).build());
        }
    }

    private void seedTemperatureHistory(Sensor sensor, double baseline) {
        Instant now = Instant.now();
        for (int i = 48; i >= 0; i--) {
            Instant recordedAt = now.minus(i * 30L, ChronoUnit.MINUTES);
            double hourOfDay = (recordedAt.toEpochMilli() / 3_600_000.0) % 24;
            double dayNightSwing = 6 * Math.sin((hourOfDay - 9) / 24 * 2 * Math.PI);
            double noise = (random.nextDouble() - 0.5) * 1.2;
            double value = baseline + dayNightSwing + noise;
            sensorReadingRepository.save(SensorReading.builder()
                    .sensor(sensor).value(round1(value)).recordedAt(recordedAt).build());
        }
    }

    private void seedAnalyticsHistory(Field field, double baseMoisture, double minTemp, double maxTemp, int cycles) {
        for (int i = 7; i >= 1; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            double moisture = baseMoisture + (random.nextDouble() - 0.5) * 4;
            double liters = cycles * 20 * 20.0 + random.nextDouble() * 50;
            analyticsSnapshotRepository.save(AnalyticsSnapshot.builder()
                    .field(field).snapshotDate(date)
                    .avgSoilMoisture(round1(moisture))
                    .minTemperature(round1(minTemp + random.nextDouble()))
                    .maxTemperature(round1(maxTemp + random.nextDouble()))
                    .totalLitersUsed(round1(liters))
                    .irrigationCycles(cycles)
                    .build());
        }
    }

    private double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
