package com.reporting.dataservice.repository;

import com.reporting.dataservice.model.FileIngestionSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FileIngestionSessionRepository extends JpaRepository<FileIngestionSession, Long> {
    
    @Query("SELECT s FROM FileIngestionSession s ORDER BY s.startTime DESC")
    List<FileIngestionSession> findAllOrderByStartTimeDesc();
    
    @Query("SELECT s FROM FileIngestionSession s WHERE s.startTime >= ?1 ORDER BY s.startTime DESC")
    List<FileIngestionSession> findRecentSessions(LocalDateTime since);
    
    @Query("SELECT AVG(s.durationMs) FROM FileIngestionSession s WHERE s.status = 'COMPLETED'")
    Double getAverageProcessingTime();
    
    @Query("SELECT SUM(s.totalRecords) FROM FileIngestionSession s WHERE s.startTime >= ?1")
    Long getTotalRecordsProcessedSince(LocalDateTime since);
}
