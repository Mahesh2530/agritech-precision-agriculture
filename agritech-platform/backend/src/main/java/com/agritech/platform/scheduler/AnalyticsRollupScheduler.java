package com.agritech.platform.scheduler;

import com.agritech.platform.repository.FieldRepository;
import com.agritech.platform.service.analytics.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class AnalyticsRollupScheduler {

    private final FieldRepository fieldRepository;
    private final AnalyticsService analyticsService;

    /** Runs nightly at 00:10 to roll up the previous day's data per field. */
    @Scheduled(cron = "${app.scheduler.analytics-cron:0 10 0 * * *}")
    public void rollupYesterday() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        fieldRepository.findAll().forEach(field -> analyticsService.rollupField(field, yesterday));
    }
}
