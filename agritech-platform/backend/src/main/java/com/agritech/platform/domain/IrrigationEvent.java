package com.agritech.platform.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "irrigation_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IrrigationEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private IrrigationDevice device;

    @Column(nullable = false)
    private Instant startedAt;

    private Instant endedAt;

    private Double litersUsed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TriggerSource triggeredBy;

    @Builder.Default
    private boolean stillRunning = true;
}
