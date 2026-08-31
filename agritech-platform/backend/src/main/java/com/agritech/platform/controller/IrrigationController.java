package com.agritech.platform.controller;

import com.agritech.platform.domain.IrrigationDevice;
import com.agritech.platform.domain.IrrigationEvent;
import com.agritech.platform.dto.IrrigationDtos.*;
import com.agritech.platform.repository.IrrigationDeviceRepository;
import com.agritech.platform.repository.IrrigationEventRepository;
import com.agritech.platform.service.irrigation.IrrigationAutomationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/irrigation")
@RequiredArgsConstructor
public class IrrigationController {

    private final IrrigationDeviceRepository irrigationDeviceRepository;
    private final IrrigationEventRepository irrigationEventRepository;
    private final IrrigationAutomationService irrigationAutomationService;

    @GetMapping("/fields/{fieldId}/devices")
    public List<DeviceResponse> devicesForField(@PathVariable Long fieldId) {
        return irrigationDeviceRepository.findByFieldId(fieldId).stream().map(this::toResponse).toList();
    }

    @PostMapping("/devices/{deviceId}/command")
    public ResponseEntity<?> command(@PathVariable Long deviceId, @Valid @RequestBody CommandRequest request) {
        IrrigationDevice device = irrigationDeviceRepository.findById(deviceId).orElseThrow();
        IrrigationEvent event = irrigationAutomationService.manualCommand(device, request.activate(), request.durationMinutes());
        return ResponseEntity.ok(event != null ? toEventResponse(event) : toResponse(device));
    }

    @GetMapping("/devices/{deviceId}/events")
    public List<EventResponse> events(@PathVariable Long deviceId) {
        return irrigationEventRepository.findByDeviceIdOrderByStartedAtDesc(deviceId).stream()
                .map(this::toEventResponse).toList();
    }

    private DeviceResponse toResponse(IrrigationDevice d) {
        return new DeviceResponse(d.getId(), d.getDeviceCode(), d.getName(), d.isActive(),
                d.getFlowRateLitersPerMinute(), d.getField().getId());
    }

    private EventResponse toEventResponse(IrrigationEvent e) {
        return new EventResponse(e.getId(), e.getDevice().getId(),
                e.getStartedAt() != null ? e.getStartedAt().toString() : null,
                e.getEndedAt() != null ? e.getEndedAt().toString() : null,
                e.getLitersUsed(), e.getTriggeredBy(), e.isStillRunning());
    }
}
