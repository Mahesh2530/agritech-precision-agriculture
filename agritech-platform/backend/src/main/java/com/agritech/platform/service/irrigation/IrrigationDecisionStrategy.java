package com.agritech.platform.service.irrigation;

import com.agritech.platform.domain.Field;
import com.agritech.platform.domain.SensorReading;

import java.util.List;

/**
 * Pluggable decision strategy. ThresholdIrrigationStrategy is active by default (v1).
 * MlIrrigationStrategy is a ready-to-wire stub for Phase 3 (enable via
 * app.irrigation.strategy=ml in application.yml once an inference service exists).
 */
public interface IrrigationDecisionStrategy {
    IrrigationDecision decide(Field field, List<SensorReading> recentSoilMoistureReadings);
}
