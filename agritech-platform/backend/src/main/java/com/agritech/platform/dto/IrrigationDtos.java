package com.agritech.platform.dto;

import com.agritech.platform.domain.TriggerSource;
import jakarta.validation.constraints.NotNull;

public class IrrigationDtos {
    public record CommandRequest(@NotNull Boolean activate, Integer durationMinutes) {}

    public record DeviceResponse(Long id, String deviceCode, String name, boolean active,
                                  Double flowRateLitersPerMinute, Long fieldId) {}

    public record EventResponse(Long id, Long deviceId, String startedAt, String endedAt,
                                 Double litersUsed, TriggerSource triggeredBy, boolean stillRunning) {}
}
