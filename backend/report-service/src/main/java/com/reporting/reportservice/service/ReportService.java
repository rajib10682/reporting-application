package com.reporting.reportservice.service;

import com.reporting.reportservice.model.Report;
import com.reporting.reportservice.model.ReportData;
import com.reporting.reportservice.repository.ReportRepository;
import com.reporting.reportservice.repository.ReportDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReportDataRepository reportDataRepository;

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public Report getReportById(Long id) {
        Optional<Report> report = reportRepository.findById(id);
        return report.orElse(null);
    }

    public Report createReport(Report report) {
        Report savedReport = reportRepository.save(report);
        generateSampleData(savedReport.getId());
        return savedReport;
    }

    public List<ReportData> getReportData(Long reportId) {
        return reportDataRepository.findByReportId(reportId);
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalReports", reportRepository.count());
        stats.put("totalUsers", 25);
        stats.put("totalRevenue", 125000.50);
        stats.put("activeReports", reportRepository.count());
        
        List<Map<String, Object>> chartData = Arrays.asList(
            Map.of("name", "Jan", "value", 4000),
            Map.of("name", "Feb", "value", 3000),
            Map.of("name", "Mar", "value", 2000),
            Map.of("name", "Apr", "value", 2780),
            Map.of("name", "May", "value", 1890),
            Map.of("name", "Jun", "value", 2390)
        );
        stats.put("chartData", chartData);
        
        return stats;
    }

    public boolean deleteReport(Long id) {
        if (reportRepository.existsById(id)) {
            reportDataRepository.deleteByReportId(id);
            reportRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private void generateSampleData(Long reportId) {
        List<ReportData> sampleData = Arrays.asList(
            new ReportData(reportId, "Sales", 15000.0, "Q1 Sales", "2024-Q1"),
            new ReportData(reportId, "Sales", 18000.0, "Q2 Sales", "2024-Q2"),
            new ReportData(reportId, "Marketing", 5000.0, "Q1 Marketing", "2024-Q1"),
            new ReportData(reportId, "Marketing", 7000.0, "Q2 Marketing", "2024-Q2"),
            new ReportData(reportId, "Operations", 12000.0, "Q1 Operations", "2024-Q1"),
            new ReportData(reportId, "Operations", 14000.0, "Q2 Operations", "2024-Q2")
        );
        reportDataRepository.saveAll(sampleData);
    }
}
