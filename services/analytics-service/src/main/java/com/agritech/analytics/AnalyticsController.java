package com.agritech.analytics;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @GetMapping("/fields/{fieldId}/summary")
    public Map<String, Object> summary(@PathVariable Long fieldId) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("fieldId", fieldId);
        summary.put("avgMoisture", 42.7);
        summary.put("waterUsageLiters", 1320);
        summary.put("irrigationCycles", 4);
        summary.put("status", "healthy");
        return summary;
    }
}
