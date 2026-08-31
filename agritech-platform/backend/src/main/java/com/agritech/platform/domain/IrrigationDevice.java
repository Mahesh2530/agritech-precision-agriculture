package com.agritech.platform.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "irrigation_devices")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IrrigationDevice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String deviceCode;

    @Column(nullable = false)
    private String name;

    @Builder.Default
    private boolean active = false;

    @Builder.Default
    private Double flowRateLitersPerMinute = 20.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private Field field;
}
