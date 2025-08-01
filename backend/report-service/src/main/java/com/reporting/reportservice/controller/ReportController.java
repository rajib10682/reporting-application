package com.reporting.reportservice.controller;

import com.reporting.reportservice.dto.QueryConfig;
import com.reporting.reportservice.model.*;
import com.reporting.reportservice.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {
    
    @Autowired
    private ReportService reportService;
    
    @Autowired
    private QueryBuilderService queryBuilderService;
    
    @Autowired
    private QueueManagerService queueManagerService;
    
    @Autowired
    private MetadataService metadataService;
    
    @Autowired
    private BusinessQueryBuilderService businessQueryBuilderService;

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
    
    @PostMapping("/build")
    public ResponseEntity<?> buildReport(@RequestBody Map<String, Object> requestBody) {
        try {
            Long userId = Long.valueOf(requestBody.get("userId").toString());
            String reportName = (String) requestBody.get("reportName");
            QueryConfig queryConfig = (QueryConfig) requestBody.get("queryConfig");
            
            if (!queryBuilderService.validateQuery(queryConfig)) {
                return ResponseEntity.badRequest().body("Invalid query configuration");
            }
            
            ReportRequest reportRequest = new ReportRequest(userId, reportName, queryConfig.toString());
            reportRequest = queueManagerService.submitReport(reportRequest);
            
            return ResponseEntity.ok(reportRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing report request: " + e.getMessage());
        }
    }
    
    @PostMapping("/preview")
    public ResponseEntity<?> previewQuery(@RequestBody QueryConfig queryConfig) {
        try {
            String sqlQuery = queryBuilderService.buildQuery(queryConfig);
            return ResponseEntity.ok(Map.of("sql", sqlQuery));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error building query: " + e.getMessage());
        }
    }
    
    @GetMapping("/queue/status")
    public ResponseEntity<List<ReportRequest>> getQueueStatus() {
        List<ReportRequest> queueStatus = queueManagerService.getQueueStatus();
        return ResponseEntity.ok(queueStatus);
    }
    
    @GetMapping("/queue/stats")
    public ResponseEntity<Map<String, Object>> getQueueStats() {
        Map<String, Object> stats = Map.of(
            "executing", queueManagerService.getCurrentExecutingCount(),
            "queued", queueManagerService.getQueueLength()
        );
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReportRequest>> getUserReports(@PathVariable Long userId) {
        List<ReportRequest> userReports = queueManagerService.getUserReports(userId);
        return ResponseEntity.ok(userReports);
    }
    
    @GetMapping("/request/{reportId}/position")
    public ResponseEntity<Map<String, Object>> getQueuePosition(@PathVariable Long reportId) {
        Long position = queueManagerService.getQueuePosition(reportId);
        return ResponseEntity.ok(Map.of("position", position != null ? position : -1));
    }
    
    @GetMapping("/metadata/tables")
    public ResponseEntity<List<TableMetadata>> getAvailableTables() {
        List<TableMetadata> tables = metadataService.getAllActiveTables();
        return ResponseEntity.ok(tables);
    }
    
    @GetMapping("/metadata/tables/{tableName}/columns")
    public ResponseEntity<List<ColumnMetadata>> getTableColumns(@PathVariable String tableName) {
        List<ColumnMetadata> columns = metadataService.getSelectableColumns(tableName);
        return ResponseEntity.ok(columns);
    }
    
    @GetMapping("/metadata/tables/{tableName}/filters")
    public ResponseEntity<List<ColumnMetadata>> getFilterableColumns(@PathVariable String tableName) {
        List<ColumnMetadata> columns = metadataService.getFilterableColumns(tableName);
        return ResponseEntity.ok(columns);
    }
    
    @GetMapping("/metadata/tables/{tableName}/joins")
    public ResponseEntity<List<JoinMetadata>> getAvailableJoins(@PathVariable String tableName) {
        List<JoinMetadata> joins = metadataService.getAvailableJoins(tableName);
        return ResponseEntity.ok(joins);
    }
    
    @GetMapping("/templates")
    public ResponseEntity<List<ReportTemplate>> getTemplates(@RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(List.of());
    }
    
    @PostMapping("/templates")
    public ResponseEntity<ReportTemplate> saveTemplate(@RequestBody ReportTemplate template) {
        return ResponseEntity.ok(template);
    }
    
    @PostMapping("/metadata/initialize")
    public ResponseEntity<String> initializeMetadata() {
        metadataService.initializeBusinessMetadata();
        return ResponseEntity.ok("Business metadata initialized successfully");
    }
}
