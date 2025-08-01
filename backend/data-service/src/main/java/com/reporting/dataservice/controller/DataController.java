package com.reporting.dataservice.controller;

import com.reporting.dataservice.model.DataSource;
import com.reporting.dataservice.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/data")
@CrossOrigin(origins = "*")
public class DataController {

    @Autowired
    private DataService dataService;

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
}
