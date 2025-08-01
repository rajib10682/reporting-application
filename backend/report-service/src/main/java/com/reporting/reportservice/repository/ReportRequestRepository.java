package com.reporting.reportservice.repository;

import com.reporting.reportservice.model.ReportRequest;
import com.reporting.reportservice.model.ReportRequest.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportRequestRepository extends JpaRepository<ReportRequest, Long> {
    
    List<ReportRequest> findByUserId(Long userId);
    
    List<ReportRequest> findByStatus(ReportStatus status);
    
    @Query("SELECT r FROM ReportRequest r WHERE r.status IN :statuses ORDER BY r.priority DESC, r.createdAt ASC")
    List<ReportRequest> findByStatusInOrderByPriorityDescCreatedAtAsc(@Param("statuses") List<ReportStatus> statuses);
    
    @Query("SELECT COUNT(r) FROM ReportRequest r WHERE r.status = :status")
    Long countByStatus(@Param("status") ReportStatus status);
    
    @Query("SELECT COUNT(r) FROM ReportRequest r WHERE r.userId = :userId AND r.status = :status")
    Long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") ReportStatus status);
    
    @Query("SELECT r FROM ReportRequest r WHERE r.status IN ('PENDING', 'QUEUED') ORDER BY r.priority DESC, r.createdAt ASC")
    List<ReportRequest> findQueuedReports();
}
