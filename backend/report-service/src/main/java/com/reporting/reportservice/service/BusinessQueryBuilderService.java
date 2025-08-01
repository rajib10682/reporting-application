package com.reporting.reportservice.service;

import com.reporting.reportservice.dto.QueryConfig;
import com.reporting.reportservice.model.TableMetadata;
import com.reporting.reportservice.model.ColumnMetadata;
import com.reporting.reportservice.model.JoinMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BusinessQueryBuilderService {
    
    @Autowired
    private MetadataService metadataService;
    
    @Autowired
    private QueryBuilderService queryBuilderService;
    
    public String buildFXRateQuery(Map<String, Object> config) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        
        List<String> selectedColumns = (List<String>) config.get("selectedColumns");
        if (selectedColumns != null && !selectedColumns.isEmpty()) {
            sql.append(String.join(", ", selectedColumns));
        } else {
            sql.append("f.fx_id, f.fx_name, f.year, f.currency");
            for (int i = 1; i <= 12; i++) {
                sql.append(", f.m").append(i).append("_rate");
            }
        }
        
        sql.append(" FROM fxrate_info f");
        
        Map<String, Object> filters = (Map<String, Object>) config.get("filters");
        if (filters != null && !filters.isEmpty()) {
            sql.append(" WHERE ");
            List<String> conditions = filters.entrySet().stream()
                .map(entry -> buildFilterCondition("f." + entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
            sql.append(String.join(" AND ", conditions));
        }
        
        String orderBy = (String) config.get("orderBy");
        if (orderBy != null && !orderBy.isEmpty()) {
            sql.append(" ORDER BY ").append(orderBy);
        }
        
        return sql.toString();
    }
    
    public String buildScenarioWithFXRateQuery(Map<String, Object> config) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        
        List<String> selectedColumns = (List<String>) config.get("selectedColumns");
        if (selectedColumns != null && !selectedColumns.isEmpty()) {
            sql.append(String.join(", ", selectedColumns));
        } else {
            sql.append("s.scenario_id, s.scenario_name, s.start_date, s.end_date, ");
            sql.append("f.fx_id, f.fx_name, f.currency, f.year");
        }
        
        sql.append(" FROM scenario_info s");
        sql.append(" LEFT JOIN fxrate_info f ON s.fx_rate = f.fx_id");
        
        Map<String, Object> filters = (Map<String, Object>) config.get("filters");
        if (filters != null && !filters.isEmpty()) {
            sql.append(" WHERE ");
            List<String> conditions = filters.entrySet().stream()
                .map(entry -> {
                    String column = entry.getKey();
                    if (column.startsWith("fx_") || column.equals("currency") || column.equals("year") || column.startsWith("m")) {
                        return buildFilterCondition("f." + column, entry.getValue());
                    } else {
                        return buildFilterCondition("s." + column, entry.getValue());
                    }
                })
                .collect(Collectors.toList());
            sql.append(String.join(" AND ", conditions));
        }
        
        String orderBy = (String) config.get("orderBy");
        if (orderBy != null && !orderBy.isEmpty()) {
            sql.append(" ORDER BY ").append(orderBy);
        }
        
        return sql.toString();
    }
    
    public String buildGOCAnalysisQuery(Map<String, Object> config) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        
        List<String> selectedColumns = (List<String>) config.get("selectedColumns");
        if (selectedColumns != null && !selectedColumns.isEmpty()) {
            sql.append(String.join(", ", selectedColumns));
        } else {
            sql.append("g.goc, g.segment_id, g.geo_id, g.period_id, ");
            sql.append("s.segment_parent_id, geo.geo_parent_id");
        }
        
        sql.append(" FROM goc_info g");
        sql.append(" INNER JOIN segment_info s ON (g.segment_id = s.segment_id AND g.period_id = s.period_id)");
        sql.append(" INNER JOIN geography_info geo ON (g.geo_id = geo.geo_id AND g.period_id = geo.period_id)");
        
        Map<String, Object> filters = (Map<String, Object>) config.get("filters");
        if (filters != null && !filters.isEmpty()) {
            sql.append(" WHERE ");
            List<String> conditions = filters.entrySet().stream()
                .map(entry -> {
                    String column = entry.getKey();
                    if (column.equals("segment_parent_id")) {
                        return buildFilterCondition("s." + column, entry.getValue());
                    } else if (column.equals("geo_parent_id")) {
                        return buildFilterCondition("geo." + column, entry.getValue());
                    } else {
                        return buildFilterCondition("g." + column, entry.getValue());
                    }
                })
                .collect(Collectors.toList());
            sql.append(String.join(" AND ", conditions));
        }
        
        String orderBy = (String) config.get("orderBy");
        if (orderBy != null && !orderBy.isEmpty()) {
            sql.append(" ORDER BY ").append(orderBy);
        }
        
        return sql.toString();
    }
    
    public String buildHierarchicalQuery(String tableName, Map<String, Object> config) {
        StringBuilder sql = new StringBuilder();
        
        Boolean includeHierarchy = (Boolean) config.get("includeHierarchy");
        if (includeHierarchy != null && includeHierarchy) {
            sql.append("WITH RECURSIVE hierarchy AS (");
            sql.append("  SELECT ").append(getHierarchyColumns(tableName)).append(", 0 as level");
            sql.append("  FROM ").append(tableName);
            sql.append("  WHERE ").append(getParentColumn(tableName)).append(" = 'ROOT'");
            sql.append("  UNION ALL");
            sql.append("  SELECT ").append(getHierarchyColumns(tableName)).append(", h.level + 1");
            sql.append("  FROM ").append(tableName).append(" t");
            sql.append("  INNER JOIN hierarchy h ON t.").append(getParentColumn(tableName));
            sql.append(" = h.").append(getIdColumn(tableName));
            sql.append(") SELECT * FROM hierarchy");
        } else {
            sql.append("SELECT ");
            List<String> selectedColumns = (List<String>) config.get("selectedColumns");
            if (selectedColumns != null && !selectedColumns.isEmpty()) {
                sql.append(String.join(", ", selectedColumns));
            } else {
                sql.append("*");
            }
            sql.append(" FROM ").append(tableName);
        }
        
        Map<String, Object> filters = (Map<String, Object>) config.get("filters");
        if (filters != null && !filters.isEmpty()) {
            String whereClause = includeHierarchy != null && includeHierarchy ? "" : " WHERE ";
            if (includeHierarchy != null && includeHierarchy) {
                sql.append(" WHERE ");
            } else {
                sql.append(whereClause);
            }
            
            List<String> conditions = filters.entrySet().stream()
                .map(entry -> buildFilterCondition(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
            sql.append(String.join(" AND ", conditions));
        }
        
        String orderBy = (String) config.get("orderBy");
        if (orderBy != null && !orderBy.isEmpty()) {
            sql.append(" ORDER BY ").append(orderBy);
        }
        
        return sql.toString();
    }
    
    private String buildFilterCondition(String column, Object value) {
        if (value instanceof List) {
            List<?> values = (List<?>) value;
            String inClause = values.stream()
                .map(v -> "'" + v.toString() + "'")
                .collect(Collectors.joining(", "));
            return column + " IN (" + inClause + ")";
        } else if (value instanceof Map) {
            Map<String, Object> rangeFilter = (Map<String, Object>) value;
            if (rangeFilter.containsKey("from") && rangeFilter.containsKey("to")) {
                return column + " BETWEEN '" + rangeFilter.get("from") + "' AND '" + rangeFilter.get("to") + "'";
            }
        }
        return column + " = '" + value.toString() + "'";
    }
    
    private String getHierarchyColumns(String tableName) {
        switch (tableName) {
            case "account_info":
                return "account_id, account_parent_id, period_id";
            case "segment_info":
                return "segment_id, segment_parent_id, period_id";
            case "geography_info":
                return "geo_id, geo_parent_id, period_id";
            default:
                return "*";
        }
    }
    
    private String getIdColumn(String tableName) {
        switch (tableName) {
            case "account_info":
                return "account_id";
            case "segment_info":
                return "segment_id";
            case "geography_info":
                return "geo_id";
            default:
                return "id";
        }
    }
    
    private String getParentColumn(String tableName) {
        switch (tableName) {
            case "account_info":
                return "account_parent_id";
            case "segment_info":
                return "segment_parent_id";
            case "geography_info":
                return "geo_parent_id";
            default:
                return "parent_id";
        }
    }
    
    public boolean validateBusinessQuery(Map<String, Object> config) {
        String queryType = (String) config.get("queryType");
        if (queryType == null) {
            return false;
        }
        
        switch (queryType) {
            case "fxrate":
                return validateFXRateQuery(config);
            case "scenario_fxrate":
                return validateScenarioFXRateQuery(config);
            case "goc_analysis":
                return validateGOCAnalysisQuery(config);
            case "hierarchical":
                return validateHierarchicalQuery(config);
            default:
                return false;
        }
    }
    
    private boolean validateFXRateQuery(Map<String, Object> config) {
        Map<String, Object> filters = (Map<String, Object>) config.get("filters");
        if (filters != null) {
            for (String key : filters.keySet()) {
                if (!isValidFXRateColumn(key)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean validateScenarioFXRateQuery(Map<String, Object> config) {
        Map<String, Object> filters = (Map<String, Object>) config.get("filters");
        if (filters != null) {
            for (String key : filters.keySet()) {
                if (!isValidScenarioColumn(key) && !isValidFXRateColumn(key)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean validateGOCAnalysisQuery(Map<String, Object> config) {
        Map<String, Object> filters = (Map<String, Object>) config.get("filters");
        if (filters != null) {
            for (String key : filters.keySet()) {
                if (!isValidGOCColumn(key) && !isValidSegmentColumn(key) && !isValidGeographyColumn(key)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean validateHierarchicalQuery(Map<String, Object> config) {
        String tableName = (String) config.get("tableName");
        return tableName != null && (tableName.equals("account_info") || 
                                   tableName.equals("segment_info") || 
                                   tableName.equals("geography_info"));
    }
    
    private boolean isValidFXRateColumn(String column) {
        return column.equals("fx_id") || column.equals("fx_name") || 
               column.equals("year") || column.equals("currency") ||
               column.matches("m\\d+_rate");
    }
    
    private boolean isValidScenarioColumn(String column) {
        return column.equals("scenario_id") || column.equals("scenario_name") ||
               column.equals("start_date") || column.equals("end_date") ||
               column.equals("fx_rate");
    }
    
    private boolean isValidGOCColumn(String column) {
        return column.equals("goc") || column.equals("segment_id") ||
               column.equals("geo_id") || column.equals("period_id");
    }
    
    private boolean isValidSegmentColumn(String column) {
        return column.equals("segment_id") || column.equals("segment_parent_id") ||
               column.equals("period_id");
    }
    
    private boolean isValidGeographyColumn(String column) {
        return column.equals("geo_id") || column.equals("geo_parent_id") ||
               column.equals("period_id");
    }
}
