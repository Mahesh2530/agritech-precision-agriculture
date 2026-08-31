package com.agritech.platform.dto;

import java.time.LocalDate;
import java.util.List;

public class AnalyticsDtos {
    public record DailyPoint(LocalDate date, Double avgSoilMoisture, Double minTemperature,
                              Double maxTemperature, Double totalLitersUsed, Integer irrigationCycles) {}

    public record FieldSummary(Long fieldId, String fieldName, List<DailyPoint> series,
                                Double totalWaterUsedLiters, Double avgMoisture) {}
}
