package com.agritech.platform.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sensors")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Sensor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String deviceCode;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetricType metricType;

    private String unit;

    @Builder.Default
    private boolean online = true;

    private java.time.Instant lastSeenAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private Field field;
}
