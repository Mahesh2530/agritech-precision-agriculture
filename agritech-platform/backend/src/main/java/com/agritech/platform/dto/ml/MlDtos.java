package com.agritech.platform.dto.ml;

import java.util.List;
import java.util.Map;

/**
 * Contract for the pluggable ML irrigation strategy (Phase 3 roadmap).
 * Kept isolated so a future inference service integration only touches this file
 * and MlIrrigationStrategy, never the controllers or scheduler.
 */
public class MlDtos {

    public record InferenceRequest(Long fieldId, Map<String, Double> latestMetrics,
                                    List<Double> soilMoistureHistory, Double forecastRainMm) {}

    public record InferenceResponse(boolean shouldIrrigate, Integer recommendedMinutes, Double confidence) {}
}
