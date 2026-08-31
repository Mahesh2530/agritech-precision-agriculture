package com.agritech.platform.service.irrigation;

import com.agritech.platform.domain.Field;
import com.agritech.platform.domain.SensorReading;
import com.agritech.platform.domain.TriggerSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "app.irrigation.strategy", havingValue = "threshold", matchIfMissing = true)
public class ThresholdIrrigationStrategy implements IrrigationDecisionStrategy {

    @Override
    public IrrigationDecision decide(Field field, List<SensorReading> recentSoilMoistureReadings) {
        if (recentSoilMoistureReadings.isEmpty()) {
            return new IrrigationDecision(false, 0, TriggerSource.RULE, "No recent soil moisture data");
        }
        double latest = recentSoilMoistureReadings.get(recentSoilMoistureReadings.size() - 1).getValue();
        double threshold = field.getMoistureThreshold() != null ? field.getMoistureThreshold() : 30.0;

        if (latest < threshold) {
            int minutes = field.getDefaultIrrigationMinutes() != null ? field.getDefaultIrrigationMinutes() : 15;
            String reason = String.format("Soil moisture %.1f%% below threshold %.1f%%", latest, threshold);
            return new IrrigationDecision(true, minutes, TriggerSource.RULE, reason);
        }
        return new IrrigationDecision(false, 0, TriggerSource.RULE,
                String.format("Soil moisture %.1f%% is at/above threshold %.1f%%", latest, threshold));
    }
}
