package com.agritech.platform.controller;

import com.agritech.platform.domain.Alert;
import com.agritech.platform.dto.AlertDtos.AlertResponse;
import com.agritech.platform.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertRepository alertRepository;

    @GetMapping
    public List<AlertResponse> list(@RequestParam(required = false) Long farmId,
                                     @RequestParam(required = false) Boolean unacknowledgedOnly) {
        List<Alert> alerts = farmId != null ? alertRepository.findByFarmId(farmId)
                : Boolean.TRUE.equals(unacknowledgedOnly) ? alertRepository.findByAcknowledgedFalseOrderByCreatedAtDesc()
                : alertRepository.findAll();
        return alerts.stream().map(this::toResponse).toList();
    }

    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<AlertResponse> acknowledge(@PathVariable Long id) {
        Alert alert = alertRepository.findById(id).orElseThrow();
        alert.setAcknowledged(true);
        alertRepository.save(alert);
        return ResponseEntity.ok(toResponse(alert));
    }

    private AlertResponse toResponse(Alert a) {
        return new AlertResponse(a.getId(), a.getField().getId(), a.getField().getName(),
                a.getType(), a.getSeverity(), a.getMessage(), a.isAcknowledged(), a.getCreatedAt());
    }
}
