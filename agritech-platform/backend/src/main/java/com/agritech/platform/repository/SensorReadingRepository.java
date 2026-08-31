package com.agritech.platform.repository;

import com.agritech.platform.domain.MetricType;
import com.agritech.platform.domain.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;

public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {

    @Query("select r from SensorReading r where r.sensor.field.id = :fieldId " +
           "and r.sensor.metricType = :metricType and r.recordedAt between :from and :to " +
           "order by r.recordedAt asc")
    List<SensorReading> findHistory(@Param("fieldId") Long fieldId,
                                     @Param("metricType") MetricType metricType,
                                     @Param("from") Instant from,
                                     @Param("to") Instant to);

    @Query("select r from SensorReading r where r.sensor.id = :sensorId order by r.recordedAt desc limit :limit")
    List<SensorReading> findRecentBySensor(@Param("sensorId") Long sensorId, @Param("limit") int limit);

    @Query("select r from SensorReading r where r.sensor.field.id = :fieldId order by r.recordedAt desc limit 1")
    List<SensorReading> findLatestForField(@Param("fieldId") Long fieldId);
}
