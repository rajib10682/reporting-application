package com.reporting.dataservice.repository;

import com.reporting.dataservice.model.AdjustmentSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AdjustmentSessionRepository extends JpaRepository<AdjustmentSession, Long> {
    List<AdjustmentSession> findBySubmittedByOrderByCreatedAtDesc(Long submittedBy);
    List<AdjustmentSession> findByStatusOrderByCreatedAtDesc(String status);
    List<AdjustmentSession> findByScenarioIdOrderByCreatedAtDesc(Long scenarioId);
    List<AdjustmentSession> findAllByOrderByCreatedAtDesc();
}
