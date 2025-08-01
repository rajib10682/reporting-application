package com.reporting.reportservice.service;

import com.reporting.reportservice.dto.QueryConfig;
import com.reporting.reportservice.model.JoinMetadata;
import com.reporting.reportservice.model.TableMetadata;
import com.reporting.reportservice.repository.JoinMetadataRepository;
import com.reporting.reportservice.repository.TableMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QueryBuilderService {
    
    @Autowired
    private TableMetadataRepository tableMetadataRepository;
    
    @Autowired
    private JoinMetadataRepository joinMetadataRepository;
    
    public String buildQuery(QueryConfig config) {
        StringBuilder query = new StringBuilder();
        
        query.append("SELECT ");
        if (config.getSelectedColumns() != null && !config.getSelectedColumns().isEmpty()) {
            String selectClause = config.getSelectedColumns().stream()
                .map(this::buildColumnSelection)
                .collect(Collectors.joining(", "));
            query.append(selectClause);
        } else {
            query.append("*");
        }
        
        query.append(" FROM ");
        String fromClause = buildFromClause(config.getSelectedTables());
        query.append(fromClause);
        
        if (config.getFilters() != null && !config.getFilters().isEmpty()) {
            query.append(" WHERE ");
            String whereClause = buildWhereClause(config.getFilters());
            query.append(whereClause);
        }
        
        if (config.getSorting() != null && !config.getSorting().isEmpty()) {
            query.append(" ORDER BY ");
            String orderByClause = config.getSorting().stream()
                .map(sort -> sort.getTableName() + "." + sort.getColumnName() + " " + sort.getDirection())
                .collect(Collectors.joining(", "));
            query.append(orderByClause);
        }
        
        if (config.getLimit() != null && config.getLimit() > 0) {
            query.append(" LIMIT ").append(config.getLimit());
        }
        
        return query.toString();
    }
    
    private String buildColumnSelection(QueryConfig.ColumnSelection column) {
        StringBuilder columnStr = new StringBuilder();
        columnStr.append(column.getTableName()).append(".").append(column.getColumnName());
        
        if (column.getAggregation() != null && !column.getAggregation().isEmpty()) {
            columnStr = new StringBuilder(column.getAggregation() + "(" + columnStr + ")");
        }
        
        if (column.getAlias() != null && !column.getAlias().isEmpty()) {
            columnStr.append(" AS ").append(column.getAlias());
        }
        
        return columnStr.toString();
    }
    
    private String buildFromClause(List<String> tableNames) {
        if (tableNames == null || tableNames.isEmpty()) {
            throw new IllegalArgumentException("At least one table must be selected");
        }
        
        if (tableNames.size() == 1) {
            return tableNames.get(0);
        }
        
        List<JoinPath> joinPaths = findJoinPaths(tableNames);
        
        StringBuilder fromClause = new StringBuilder();
        fromClause.append(tableNames.get(0)); // Start with first table
        
        Set<String> joinedTables = new HashSet<>();
        joinedTables.add(tableNames.get(0));
        
        for (JoinPath joinPath : joinPaths) {
            if (!joinedTables.contains(joinPath.targetTable)) {
                fromClause.append(" ")
                    .append(joinPath.joinType)
                    .append(" JOIN ")
                    .append(joinPath.targetTable)
                    .append(" ON ")
                    .append(joinPath.sourceTable).append(".").append(joinPath.sourceColumn)
                    .append(" = ")
                    .append(joinPath.targetTable).append(".").append(joinPath.targetColumn);
                
                joinedTables.add(joinPath.targetTable);
            }
        }
        
        return fromClause.toString();
    }
    
    private List<JoinPath> findJoinPaths(List<String> tableNames) {
        List<JoinPath> joinPaths = new ArrayList<>();
        
        for (int i = 1; i < tableNames.size(); i++) {
            String sourceTable = tableNames.get(0);
            String targetTable = tableNames.get(i);
            
            TableMetadata sourceTableMeta = tableMetadataRepository.findByTableName(sourceTable);
            TableMetadata targetTableMeta = tableMetadataRepository.findByTableName(targetTable);
            
            if (sourceTableMeta != null && targetTableMeta != null) {
                List<JoinMetadata> joins = joinMetadataRepository.findJoinsBetweenTables(
                    sourceTableMeta.getId(), targetTableMeta.getId());
                
                if (!joins.isEmpty()) {
                    JoinMetadata join = joins.get(0); // Use first available join
                    joinPaths.add(new JoinPath(
                        sourceTable, targetTable,
                        join.getSourceColumn(), join.getTargetColumn(),
                        join.getJoinType()
                    ));
                }
            }
        }
        
        return joinPaths;
    }
    
    private String buildWhereClause(List<QueryConfig.FilterCriteria> filters) {
        StringBuilder whereClause = new StringBuilder();
        
        for (int i = 0; i < filters.size(); i++) {
            QueryConfig.FilterCriteria filter = filters.get(i);
            
            if (i > 0) {
                String logicalOp = filter.getLogicalOperator() != null ? filter.getLogicalOperator() : "AND";
                whereClause.append(" ").append(logicalOp).append(" ");
            }
            
            whereClause.append(buildFilterCondition(filter));
        }
        
        return whereClause.toString();
    }
    
    private String buildFilterCondition(QueryConfig.FilterCriteria filter) {
        StringBuilder condition = new StringBuilder();
        condition.append(filter.getTableName()).append(".").append(filter.getColumnName());
        
        switch (filter.getOperator().toUpperCase()) {
            case "=":
            case "!=":
            case ">":
            case "<":
            case ">=":
            case "<=":
                condition.append(" ").append(filter.getOperator()).append(" ");
                condition.append(formatValue(filter.getValue()));
                break;
            case "LIKE":
                condition.append(" LIKE ");
                condition.append(formatValue(filter.getValue()));
                break;
            case "IN":
                condition.append(" IN (");
                if (filter.getValue() instanceof List) {
                    List<?> values = (List<?>) filter.getValue();
                    String inValues = values.stream()
                        .map(this::formatValue)
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));
                    condition.append(inValues);
                }
                condition.append(")");
                break;
            case "BETWEEN":
                condition.append(" BETWEEN ");
                condition.append(formatValue(filter.getValue()));
                condition.append(" AND ");
                condition.append(formatValue(filter.getSecondValue()));
                break;
            case "IS NULL":
                condition.append(" IS NULL");
                break;
            case "IS NOT NULL":
                condition.append(" IS NOT NULL");
                break;
        }
        
        return condition.toString();
    }
    
    private String formatValue(Object value) {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof String) {
            return "'" + value.toString().replace("'", "''") + "'";
        }
        return value.toString();
    }
    
    public boolean validateQuery(QueryConfig config) {
        try {
            buildQuery(config);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private static class JoinPath {
        String sourceTable;
        String targetTable;
        String sourceColumn;
        String targetColumn;
        String joinType;
        
        JoinPath(String sourceTable, String targetTable, String sourceColumn, String targetColumn, String joinType) {
            this.sourceTable = sourceTable;
            this.targetTable = targetTable;
            this.sourceColumn = sourceColumn;
            this.targetColumn = targetColumn;
            this.joinType = joinType;
        }
    }
}
