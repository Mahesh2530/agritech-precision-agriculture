package com.agritech.platform.dto;

import jakarta.validation.constraints.NotBlank;

public class FarmDtos {
    public record FarmRequest(@NotBlank String name, String location, Double latitude, Double longitude) {}
    public record FarmResponse(Long id, String name, String location, Double latitude, Double longitude, int fieldCount) {}
}
