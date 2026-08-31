package com.agritech.platform.repository;

import com.agritech.platform.domain.AlertRule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertRuleRepository extends JpaRepository<AlertRule, Long> {
    List<AlertRule> findByFieldIdAndEnabledTrue(Long fieldId);
}
