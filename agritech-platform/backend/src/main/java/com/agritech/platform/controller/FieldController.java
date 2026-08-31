package com.agritech.platform.controller;

import com.agritech.platform.domain.Farm;
import com.agritech.platform.domain.Field;
import com.agritech.platform.dto.FieldDtos.*;
import com.agritech.platform.repository.FarmRepository;
import com.agritech.platform.repository.FieldRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farms/{farmId}/fields")
@RequiredArgsConstructor
public class FieldController {

    private final FieldRepository fieldRepository;
    private final FarmRepository farmRepository;

    @GetMapping
    public List<FieldResponse> list(@PathVariable Long farmId) {
        return fieldRepository.findByFarmId(farmId).stream().map(this::toResponse).toList();
    }

    @PostMapping
    public ResponseEntity<FieldResponse> create(@PathVariable Long farmId, @Valid @RequestBody FieldRequest request) {
        Farm farm = farmRepository.findById(farmId).orElseThrow();
        Field field = fieldRepository.save(Field.builder()
                .name(request.name()).areaHectares(request.areaHectares()).cropType(request.cropType())
                .latitude(request.latitude()).longitude(request.longitude())
                .moistureThreshold(request.moistureThreshold() != null ? request.moistureThreshold() : 30.0)
                .defaultIrrigationMinutes(request.defaultIrrigationMinutes() != null ? request.defaultIrrigationMinutes() : 15)
                .farm(farm)
                .build());
        return ResponseEntity.ok(toResponse(field));
    }

    private FieldResponse toResponse(Field f) {
        return new FieldResponse(f.getId(), f.getName(), f.getAreaHectares(), f.getCropType(),
                f.getLatitude(), f.getLongitude(), f.getMoistureThreshold(), f.getDefaultIrrigationMinutes(),
                f.getFarm().getId());
    }
}
