package com.reporting.reportservice.service;

import com.reporting.reportservice.dto.QueryConfig;
import com.reporting.reportservice.model.ReportRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class ReportExecutionService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private QueryBuilderService queryBuilderService;
    
    @Autowired
    private QueueManagerService queueManagerService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Value("${reporting.export.output-directory:/tmp/reports}")
    private String outputDirectory;
    
    @Value("${reporting.database.query-timeout-seconds:300}")
    private int queryTimeoutSeconds;
    
    @Value("${reporting.export.max-result-rows:100000}")
    private int maxResultRows;
    
    @Async
    public CompletableFuture<Void> executeReport(ReportRequest reportRequest) {
        try {
            QueryConfig queryConfig = objectMapper.readValue(reportRequest.getQueryConfig(), QueryConfig.class);
            
            String sqlQuery = queryBuilderService.buildQuery(queryConfig);
            
            jdbcTemplate.setQueryTimeout(queryTimeoutSeconds);
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sqlQuery);
            
            if (results.size() > maxResultRows) {
                queueManagerService.completeReport(reportRequest.getId(), null, 
                    "Result set too large: " + results.size() + " rows (max: " + maxResultRows + ")");
                return CompletableFuture.completedFuture(null);
            }
            
            String filePath = exportResults(reportRequest, results, queryConfig.getExportFormat());
            
            queueManagerService.completeReport(reportRequest.getId(), filePath, null);
            
        } catch (Exception e) {
            queueManagerService.completeReport(reportRequest.getId(), null, e.getMessage());
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    private String exportResults(ReportRequest reportRequest, List<Map<String, Object>> results, String format) throws IOException {
        Path outputPath = Paths.get(outputDirectory);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }
        
        String fileName = "report_" + reportRequest.getId() + "_" + System.currentTimeMillis();
        String extension = format != null ? format.toLowerCase() : "csv";
        String filePath = outputPath.resolve(fileName + "." + extension).toString();
        
        switch (extension) {
            case "csv":
                exportToCsv(results, filePath);
                break;
            case "json":
                exportToJson(results, filePath);
                break;
            default:
                exportToCsv(results, filePath);
        }
        
        return filePath;
    }
    
    private void exportToCsv(List<Map<String, Object>> results, String filePath) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            if (!results.isEmpty()) {
                Map<String, Object> firstRow = results.get(0);
                String[] headers = firstRow.keySet().toArray(new String[0]);
                writer.writeNext(headers);
                
                for (Map<String, Object> row : results) {
                    String[] values = new String[headers.length];
                    for (int i = 0; i < headers.length; i++) {
                        Object value = row.get(headers[i]);
                        values[i] = value != null ? value.toString() : "";
                    }
                    writer.writeNext(values);
                }
            }
        }
    }
    
    private void exportToJson(List<Map<String, Object>> results, String filePath) throws IOException {
        objectMapper.writeValue(Paths.get(filePath).toFile(), results);
    }
}
