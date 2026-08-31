package com.agritech.platform.dto;

import com.agritech.platform.domain.MetricType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public class TelemetryDtos {

    /** Payload pushed by a sensor gateway. */
    public record IngestRequest(@NotBlank String deviceCode, @NotNull Double value, Instant recordedAt) {}

    public record ReadingResponse(Long sensorId, String sensorName, MetricType metricType,
                                   String unit, Double value, Instant recordedAt) {}

    public record LatestSnapshot(MetricType metricType, Double value, String unit, Instant recordedAt) {}
}
