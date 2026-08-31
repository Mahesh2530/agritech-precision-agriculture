package com.agritech.platform.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "alert_rules")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AlertRule {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private Field field;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetricType metricType;

    /** LT, GT */
    @Column(nullable = false)
    private String operator;

    @Column(nullable = false)
    private Double threshold;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity;

    @Builder.Default
    private boolean enabled = true;
}
