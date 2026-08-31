package com.agritech.platform.controller;

import com.agritech.platform.dto.AnalyticsDtos.FieldSummary;
import com.agritech.platform.service.analytics.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/fields/{fieldId}/summary")
    public FieldSummary summary(@PathVariable Long fieldId, @RequestParam(defaultValue = "14") int days) {
        return analyticsService.getSummary(fieldId, days);
    }
}
