package com.reporting.reportservice.controller;

import com.reporting.reportservice.service.MetadataService;
import com.reporting.reportservice.service.BusinessQueryBuilderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Arrays;

@RestController
@RequestMapping("/api/business-metadata")
@CrossOrigin(origins = "*")
public class BusinessMetadataController {
    
    @Autowired
    private MetadataService metadataService;
    
    @Autowired
    private BusinessQueryBuilderService businessQueryBuilderService;
    
    @GetMapping("/fxrates/currencies")
    public ResponseEntity<List<String>> getAvailableCurrencies() {
        List<String> currencies = Arrays.asList("USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY");
        return ResponseEntity.ok(currencies);
    }
    
    @GetMapping("/fxrates/years")
    public ResponseEntity<List<Integer>> getAvailableYears() {
        List<Integer> years = Arrays.asList(2020, 2021, 2022, 2023, 2024, 2025);
        return ResponseEntity.ok(years);
    }
    
    @GetMapping("/periods")
    public ResponseEntity<List<String>> getAvailablePeriods() {
        List<String> periods = Arrays.asList(
            "2024Q1", "2024Q2", "2024Q3", "2024Q4",
            "2023Q1", "2023Q2", "2023Q3", "2023Q4",
            "2022Q1", "2022Q2", "2022Q3", "2022Q4"
        );
        return ResponseEntity.ok(periods);
    }
    
    @GetMapping("/hierarchies/{tableName}")
    public ResponseEntity<Map<String, Object>> getHierarchy(
            @PathVariable String tableName, 
            @RequestParam(required = false, defaultValue = "2024Q1") String periodId) {
        
        if (!isValidHierarchicalTable(tableName)) {
            return ResponseEntity.badRequest().build();
        }
        
        Map<String, Object> config = Map.of(
            "tableName", tableName,
            "includeHierarchy", true,
            "filters", Map.of("period_id", periodId)
        );
        
        String query = businessQueryBuilderService.buildHierarchicalQuery(tableName, config);
        
        Map<String, Object> response = Map.of(
            "tableName", tableName,
            "periodId", periodId,
            "query", query,
            "columns", getHierarchyColumns(tableName)
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/validate-composite-join")
    public ResponseEntity<Map<String, Object>> validateCompositeJoin(@RequestBody Map<String, Object> request) {
        String sourceTable = (String) request.get("sourceTable");
        String targetTable = (String) request.get("targetTable");
        List<String> joinColumns = (List<String>) request.get("joinColumns");
        
        boolean isValid = validateCompositeJoinRequest(sourceTable, targetTable, joinColumns);
        
        Map<String, Object> response = Map.of(
            "valid", isValid,
            "message", isValid ? "Valid composite join" : "Invalid composite join configuration"
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/build-business-query")
    public ResponseEntity<Map<String, Object>> buildBusinessQuery(@RequestBody Map<String, Object> config) {
        try {
            if (!businessQueryBuilderService.validateBusinessQuery(config)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid query configuration"));
            }
            
            String queryType = (String) config.get("queryType");
            String sql;
            
            switch (queryType) {
                case "fxrate":
                    sql = businessQueryBuilderService.buildFXRateQuery(config);
                    break;
                case "scenario_fxrate":
                    sql = businessQueryBuilderService.buildScenarioWithFXRateQuery(config);
                    break;
                case "goc_analysis":
                    sql = businessQueryBuilderService.buildGOCAnalysisQuery(config);
                    break;
                case "hierarchical":
                    String tableName = (String) config.get("tableName");
                    sql = businessQueryBuilderService.buildHierarchicalQuery(tableName, config);
                    break;
                default:
                    return ResponseEntity.badRequest().body(Map.of("error", "Unknown query type"));
            }
            
            return ResponseEntity.ok(Map.of("sql", sql, "queryType", queryType));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error building query: " + e.getMessage()));
        }
    }
    
    @GetMapping("/table-relationships")
    public ResponseEntity<Map<String, Object>> getTableRelationships() {
        Map<String, Object> relationships = Map.of(
            "scenario_info", Map.of(
                "joins", List.of(
                    Map.of("targetTable", "fxrate_info", "joinType", "LEFT", 
                           "condition", "scenario_info.fx_rate = fxrate_info.fx_id")
                )
            ),
            "goc_info", Map.of(
                "joins", List.of(
                    Map.of("targetTable", "segment_info", "joinType", "INNER",
                           "condition", "goc_info.segment_id = segment_info.segment_id AND goc_info.period_id = segment_info.period_id"),
                    Map.of("targetTable", "geography_info", "joinType", "INNER",
                           "condition", "goc_info.geo_id = geography_info.geo_id AND goc_info.period_id = geography_info.period_id")
                )
            ),
            "hierarchical_tables", List.of("account_info", "segment_info", "geography_info")
        );
        
        return ResponseEntity.ok(relationships);
    }
    
    private boolean isValidHierarchicalTable(String tableName) {
        return tableName.equals("account_info") || 
               tableName.equals("segment_info") || 
               tableName.equals("geography_info");
    }
    
    private List<String> getHierarchyColumns(String tableName) {
        switch (tableName) {
            case "account_info":
                return Arrays.asList("account_id", "account_parent_id", "period_id");
            case "segment_info":
                return Arrays.asList("segment_id", "segment_parent_id", "period_id");
            case "geography_info":
                return Arrays.asList("geo_id", "geo_parent_id", "period_id");
            default:
                return Arrays.asList();
        }
    }
    
    private boolean validateCompositeJoinRequest(String sourceTable, String targetTable, List<String> joinColumns) {
        if (sourceTable == null || targetTable == null || joinColumns == null || joinColumns.isEmpty()) {
            return false;
        }
        
        if (sourceTable.equals("goc_info") && targetTable.equals("segment_info")) {
            return joinColumns.contains("segment_id") && joinColumns.contains("period_id");
        }
        
        if (sourceTable.equals("goc_info") && targetTable.equals("geography_info")) {
            return joinColumns.contains("geo_id") && joinColumns.contains("period_id");
        }
        
        if (sourceTable.equals("scenario_info") && targetTable.equals("fxrate_info")) {
            return joinColumns.contains("fx_rate") || joinColumns.contains("fx_id");
        }
        
        return false;
    }
}
