package com.agritech.platform.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "sensor_readings", indexes = {
        @Index(name = "idx_reading_sensor_time", columnList = "sensor_id, recordedAt")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SensorReading {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    @Column(name = "\"value\"", nullable = false)
    private Double value;

    @Column(nullable = false)
    private Instant recordedAt;
}
