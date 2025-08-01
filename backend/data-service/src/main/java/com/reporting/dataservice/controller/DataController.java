package com.reporting.dataservice.controller;

import com.reporting.dataservice.dto.IngestionStatus;
import com.reporting.dataservice.model.DataSource;
import com.reporting.dataservice.service.DataService;
import com.reporting.dataservice.service.FeedIngestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/data")
@CrossOrigin(origins = "*")
public class DataController {

    @Autowired
    private DataService dataService;
    
    @Autowired
    private FeedIngestionService feedIngestionService;

    @GetMapping("/sources")
    public List<DataSource> getAllDataSources() {
        return dataService.getAllDataSources();
    }

    @PostMapping("/sources")
    public DataSource createDataSource(@RequestBody DataSource dataSource) {
        return dataService.createDataSource(dataSource);
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalyticsData() {
        return ResponseEntity.ok(dataService.getAnalyticsData());
    }

    @GetMapping("/export/{format}")
    public ResponseEntity<String> exportData(@PathVariable String format) {
        String exportedData = dataService.exportData(format);
        return ResponseEntity.ok(exportedData);
    }
    
    @PostMapping("/feed/upload")
    public ResponseEntity<IngestionStatus> uploadFeedFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                IngestionStatus status = new IngestionStatus("FAILED", "File is empty");
                return ResponseEntity.badRequest().body(status);
            }
            
            if (!file.getOriginalFilename().endsWith(".txt")) {
                IngestionStatus status = new IngestionStatus("FAILED", "Only .txt files are supported");
                return ResponseEntity.badRequest().body(status);
            }
            
            IngestionStatus result = feedIngestionService.processFeedFile(file);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            IngestionStatus status = new IngestionStatus("FAILED", "Error processing file: " + e.getMessage());
            return ResponseEntity.internalServerError().body(status);
        }
    }
}
