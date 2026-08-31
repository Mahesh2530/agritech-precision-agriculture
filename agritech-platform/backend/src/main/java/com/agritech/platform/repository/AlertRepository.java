package com.agritech.platform.repository;

import com.agritech.platform.domain.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    @Query("select a from Alert a where a.field.farm.id = :farmId order by a.createdAt desc")
    List<Alert> findByFarmId(@Param("farmId") Long farmId);

    List<Alert> findByFieldIdOrderByCreatedAtDesc(Long fieldId);
    List<Alert> findByAcknowledgedFalseOrderByCreatedAtDesc();
}
