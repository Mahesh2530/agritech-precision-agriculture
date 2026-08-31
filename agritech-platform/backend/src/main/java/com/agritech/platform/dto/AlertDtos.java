package com.agritech.platform.dto;

import com.agritech.platform.domain.AlertSeverity;
import com.agritech.platform.domain.AlertType;
import java.time.Instant;

public class AlertDtos {
    public record AlertResponse(Long id, Long fieldId, String fieldName, AlertType type,
                                 AlertSeverity severity, String message, boolean acknowledged, Instant createdAt) {}
}
