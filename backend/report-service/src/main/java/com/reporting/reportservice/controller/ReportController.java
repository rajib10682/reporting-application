package com.reporting.reportservice.controller;

import com.reporting.reportservice.model.Report;
import com.reporting.reportservice.model.ReportData;
import com.reporting.reportservice.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping
    public List<Report> getAllReports() {
        return reportService.getAllReports();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Report> getReportById(@PathVariable Long id) {
        Report report = reportService.getReportById(id);
        return report != null ? ResponseEntity.ok(report) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public Report createReport(@RequestBody Report report) {
        return reportService.createReport(report);
    }

    @GetMapping("/{id}/data")
    public ResponseEntity<List<ReportData>> getReportData(@PathVariable Long id) {
        List<ReportData> data = reportService.getReportData(id);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/dashboard-stats")
    public ResponseEntity<Object> getDashboardStats() {
        return ResponseEntity.ok(reportService.getDashboardStats());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        boolean deleted = reportService.deleteReport(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
