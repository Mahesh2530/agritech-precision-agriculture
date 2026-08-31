package com.agritech.platform.controller;

import com.agritech.platform.domain.Farm;
import com.agritech.platform.domain.User;
import com.agritech.platform.dto.FarmDtos.*;
import com.agritech.platform.repository.FarmRepository;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farms")
@RequiredArgsConstructor
public class FarmController {

    private final FarmRepository farmRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public List<FarmResponse> list(@AuthenticationPrincipal User user) {
        return farmRepository.findByOrganizationId(user.getOrganization().getId()).stream()
                .map(this::toResponse).toList();
    }

    @PostMapping
    public ResponseEntity<FarmResponse> create(@AuthenticationPrincipal User user, @Valid @RequestBody FarmRequest request) {
        Farm farm = farmRepository.save(Farm.builder()
                .name(request.name()).location(request.location())
                .latitude(request.latitude()).longitude(request.longitude())
                .organization(user.getOrganization())
                .build());
        return ResponseEntity.ok(toResponse(farm));
    }

    private FarmResponse toResponse(Farm farm) {
        return new FarmResponse(farm.getId(), farm.getName(), farm.getLocation(),
                farm.getLatitude(), farm.getLongitude(), farm.getFields().size());
    }
}
