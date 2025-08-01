package com.reporting.reportservice.dto;

import java.util.List;
import java.util.Map;

public class QueryConfig {
    private List<String> selectedTables;
    private List<ColumnSelection> selectedColumns;
    private List<FilterCriteria> filters;
    private List<SortCriteria> sorting;
    private Integer limit;
    private String exportFormat;
    
    public static class ColumnSelection {
        private String tableName;
        private String columnName;
        private String alias;
        private String aggregation; // SUM, COUNT, AVG, etc.
        
        public ColumnSelection() {}
        
        public ColumnSelection(String tableName, String columnName, String alias) {
            this.tableName = tableName;
            this.columnName = columnName;
            this.alias = alias;
        }
        
        public String getTableName() { return tableName; }
        public void setTableName(String tableName) { this.tableName = tableName; }
        
        public String getColumnName() { return columnName; }
        public void setColumnName(String columnName) { this.columnName = columnName; }
        
        public String getAlias() { return alias; }
        public void setAlias(String alias) { this.alias = alias; }
        
        public String getAggregation() { return aggregation; }
        public void setAggregation(String aggregation) { this.aggregation = aggregation; }
    }
    
    public static class FilterCriteria {
        private String tableName;
        private String columnName;
        private String operator; // =, !=, >, <, >=, <=, LIKE, IN, BETWEEN
        private Object value;
        private Object secondValue; // for BETWEEN operator
        private String logicalOperator; // AND, OR
        
        public FilterCriteria() {}
        
        public FilterCriteria(String tableName, String columnName, String operator, Object value) {
            this.tableName = tableName;
            this.columnName = columnName;
            this.operator = operator;
            this.value = value;
        }
        
        public String getTableName() { return tableName; }
        public void setTableName(String tableName) { this.tableName = tableName; }
        
        public String getColumnName() { return columnName; }
        public void setColumnName(String columnName) { this.columnName = columnName; }
        
        public String getOperator() { return operator; }
        public void setOperator(String operator) { this.operator = operator; }
        
        public Object getValue() { return value; }
        public void setValue(Object value) { this.value = value; }
        
        public Object getSecondValue() { return secondValue; }
        public void setSecondValue(Object secondValue) { this.secondValue = secondValue; }
        
        public String getLogicalOperator() { return logicalOperator; }
        public void setLogicalOperator(String logicalOperator) { this.logicalOperator = logicalOperator; }
    }
    
    public static class SortCriteria {
        private String tableName;
        private String columnName;
        private String direction; // ASC, DESC
        
        public SortCriteria() {}
        
        public SortCriteria(String tableName, String columnName, String direction) {
            this.tableName = tableName;
            this.columnName = columnName;
            this.direction = direction;
        }
        
        public String getTableName() { return tableName; }
        public void setTableName(String tableName) { this.tableName = tableName; }
        
        public String getColumnName() { return columnName; }
        public void setColumnName(String columnName) { this.columnName = columnName; }
        
        public String getDirection() { return direction; }
        public void setDirection(String direction) { this.direction = direction; }
    }
    
    public QueryConfig() {}
    
    public List<String> getSelectedTables() { return selectedTables; }
    public void setSelectedTables(List<String> selectedTables) { this.selectedTables = selectedTables; }
    
    public List<ColumnSelection> getSelectedColumns() { return selectedColumns; }
    public void setSelectedColumns(List<ColumnSelection> selectedColumns) { this.selectedColumns = selectedColumns; }
    
    public List<FilterCriteria> getFilters() { return filters; }
    public void setFilters(List<FilterCriteria> filters) { this.filters = filters; }
    
    public List<SortCriteria> getSorting() { return sorting; }
    public void setSorting(List<SortCriteria> sorting) { this.sorting = sorting; }
    
    public Integer getLimit() { return limit; }
    public void setLimit(Integer limit) { this.limit = limit; }
    
    public String getExportFormat() { return exportFormat; }
    public void setExportFormat(String exportFormat) { this.exportFormat = exportFormat; }
}
