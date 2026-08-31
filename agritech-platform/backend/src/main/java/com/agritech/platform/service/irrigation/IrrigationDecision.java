package com.agritech.platform.service.irrigation;

import com.agritech.platform.domain.TriggerSource;

public record IrrigationDecision(boolean shouldIrrigate, int durationMinutes, TriggerSource source, String reason) {
}
