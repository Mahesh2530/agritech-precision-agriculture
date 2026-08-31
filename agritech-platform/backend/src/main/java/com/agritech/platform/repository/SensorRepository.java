package com.agritech.platform.repository;

import com.agritech.platform.domain.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, Long> {
    Optional<Sensor> findByDeviceCode(String deviceCode);
    List<Sensor> findByFieldId(Long fieldId);
    List<Sensor> findByOnlineTrueAndLastSeenAtBefore(Instant cutoff);
}
