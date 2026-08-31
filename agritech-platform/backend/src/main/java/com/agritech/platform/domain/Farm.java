package com.agritech.platform.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "farms")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Farm {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String location;
    private Double latitude;
    private Double longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @OneToMany(mappedBy = "farm", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Field> fields = new HashSet<>();
}
