package com.agritech.platform.scheduler;

import com.agritech.platform.domain.Field;
import com.agritech.platform.repository.FieldRepository;
import com.agritech.platform.service.irrigation.IrrigationAutomationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IrrigationScheduler {

    private final FieldRepository fieldRepository;
    private final IrrigationAutomationService irrigationAutomationService;

    /** Safety-net sweep in case a field hasn't received fresh telemetry to trigger inline evaluation. */
    @Scheduled(fixedRateString = "${app.scheduler.irrigation-sweep-ms:300000}")
    public void sweep() {
        for (Field field : fieldRepository.findAll()) {
            irrigationAutomationService.evaluateField(field);
        }
    }
}
