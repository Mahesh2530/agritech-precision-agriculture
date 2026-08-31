package com.agritech.platform.repository;

import com.agritech.platform.domain.AnalyticsSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface AnalyticsSnapshotRepository extends JpaRepository<AnalyticsSnapshot, Long> {
    List<AnalyticsSnapshot> findByFieldIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(
            Long fieldId, LocalDate from, LocalDate to);
}
