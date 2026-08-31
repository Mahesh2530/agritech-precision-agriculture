package com.agritech.platform.dto;

import jakarta.validation.constraints.NotBlank;

public class FieldDtos {
    public record FieldRequest(@NotBlank String name, Double areaHectares, String cropType,
                                Double latitude, Double longitude, Double moistureThreshold,
                                Integer defaultIrrigationMinutes) {}

    public record FieldResponse(Long id, String name, Double areaHectares, String cropType,
                                 Double latitude, Double longitude, Double moistureThreshold,
                                 Integer defaultIrrigationMinutes, Long farmId) {}
}
