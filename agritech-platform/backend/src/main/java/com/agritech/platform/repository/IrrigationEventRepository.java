package com.agritech.platform.repository;

import com.agritech.platform.domain.IrrigationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IrrigationEventRepository extends JpaRepository<IrrigationEvent, Long> {
    List<IrrigationEvent> findByDeviceIdOrderByStartedAtDesc(Long deviceId);
    List<IrrigationEvent> findByStillRunningTrue();
}
