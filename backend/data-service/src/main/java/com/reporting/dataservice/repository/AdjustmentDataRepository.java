package com.reporting.dataservice.repository;

import com.reporting.dataservice.model.AdjustmentData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AdjustmentDataRepository extends JpaRepository<AdjustmentData, Long> {
    List<AdjustmentData> findByAdjustmentSessionId(Long adjustmentSessionId);
    List<AdjustmentData> findByScenarioId(Long scenarioId);
    void deleteByAdjustmentSessionId(Long adjustmentSessionId);
}
