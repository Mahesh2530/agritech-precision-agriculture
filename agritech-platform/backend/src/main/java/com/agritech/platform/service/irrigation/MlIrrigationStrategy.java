package com.agritech.platform.service.irrigation;

import com.agritech.platform.domain.Field;
import com.agritech.platform.domain.SensorReading;
import com.agritech.platform.domain.TriggerSource;
import com.agritech.platform.dto.ml.MlDtos.InferenceRequest;
import com.agritech.platform.dto.ml.MlDtos.InferenceResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Phase 3 stub: delegates the irrigation decision to an external ML inference service.
 * Activate with: app.irrigation.strategy=ml and app.irrigation.ml-endpoint=<url>
 * Until that service exists, this class is simply not registered as a bean.
 */
@Component
@ConditionalOnProperty(name = "app.irrigation.strategy", havingValue = "ml")
public class MlIrrigationStrategy implements IrrigationDecisionStrategy {

    @Value("${app.irrigation.ml-endpoint:http://localhost:8500/predict}")
    private String mlEndpoint;

    private final RestClient restClient = RestClient.create();

    @Override
    public IrrigationDecision decide(Field field, List<SensorReading> recentSoilMoistureReadings) {
        List<Double> history = recentSoilMoistureReadings.stream()
                .map(SensorReading::getValue).collect(Collectors.toList());

        double latest = history.isEmpty() ? -1 : history.get(history.size() - 1);
        InferenceRequest req = new InferenceRequest(
                field.getId(), Map.of("soil_moisture", latest), history, null);

        try {
            InferenceResponse resp = restClient.post().uri(mlEndpoint)
                    .body(req).retrieve().body(InferenceResponse.class);
            if (resp == null) {
                return new IrrigationDecision(false, 0, TriggerSource.ML, "ML service returned no response");
            }
            return new IrrigationDecision(resp.shouldIrrigate(),
                    resp.recommendedMinutes() != null ? resp.recommendedMinutes() : 0,
                    TriggerSource.ML,
                    "ML model confidence " + resp.confidence());
        } catch (Exception e) {
            // Fail safe: don't irrigate if the ML service is unreachable.
            return new IrrigationDecision(false, 0, TriggerSource.ML, "ML service unavailable: " + e.getMessage());
        }
    }
}
