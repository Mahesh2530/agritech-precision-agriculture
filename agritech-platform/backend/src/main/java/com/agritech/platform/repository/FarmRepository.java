package com.agritech.platform.repository;

import com.agritech.platform.domain.Farm;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FarmRepository extends JpaRepository<Farm, Long> {
    List<Farm> findByOrganizationId(Long organizationId);
}
