package com.agritech.platform.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "fields")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Field {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private Double areaHectares;
    private String cropType;
    private Double latitude;
    private Double longitude;

    /** Soil moisture percentage below which irrigation should trigger. */
    @Builder.Default
    private Double moistureThreshold = 30.0;

    /** Default irrigation duration in minutes when triggered by the rule engine. */
    @Builder.Default
    private Integer defaultIrrigationMinutes = 15;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    @OneToMany(mappedBy = "field", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Sensor> sensors = new HashSet<>();

    @OneToMany(mappedBy = "field", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<IrrigationDevice> irrigationDevices = new HashSet<>();
}
