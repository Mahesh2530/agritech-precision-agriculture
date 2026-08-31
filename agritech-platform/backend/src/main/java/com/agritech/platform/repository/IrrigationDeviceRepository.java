package com.agritech.platform.repository;

import com.agritech.platform.domain.IrrigationDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface IrrigationDeviceRepository extends JpaRepository<IrrigationDevice, Long> {
    List<IrrigationDevice> findByFieldId(Long fieldId);
    Optional<IrrigationDevice> findByDeviceCode(String deviceCode);
}
