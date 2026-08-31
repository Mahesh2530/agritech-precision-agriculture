package com.agritech.platform.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "analytics_snapshots", uniqueConstraints = @UniqueConstraint(columnNames = {"field_id", "snapshotDate"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AnalyticsSnapshot {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private Field field;

    @Column(nullable = false)
    private LocalDate snapshotDate;

    private Double avgSoilMoisture;
    private Double minTemperature;
    private Double maxTemperature;
    private Double totalLitersUsed;
    private Integer irrigationCycles;
}
