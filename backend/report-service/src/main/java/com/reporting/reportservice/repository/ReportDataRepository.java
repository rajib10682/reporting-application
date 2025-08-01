package com.reporting.reportservice.repository;

import com.reporting.reportservice.model.ReportData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportDataRepository extends JpaRepository<ReportData, Long> {
    List<ReportData> findByReportId(Long reportId);
    void deleteByReportId(Long reportId);
}
