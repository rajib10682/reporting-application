package com.reporting.reportservice.service;

import com.reporting.reportservice.model.TableMetadata;
import com.reporting.reportservice.model.ColumnMetadata;
import com.reporting.reportservice.model.JoinMetadata;
import com.reporting.reportservice.repository.TableMetadataRepository;
import com.reporting.reportservice.repository.ColumnMetadataRepository;
import com.reporting.reportservice.repository.JoinMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

@Service
public class MetadataService {
    
    @Autowired
    private TableMetadataRepository tableMetadataRepository;
    
    @Autowired
    private ColumnMetadataRepository columnMetadataRepository;
    
    @Autowired
    private JoinMetadataRepository joinMetadataRepository;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public List<TableMetadata> getAllActiveTables() {
        return tableMetadataRepository.findByIsActiveTrue();
    }
    
    public List<TableMetadata> getAllTablesWithColumns() {
        return tableMetadataRepository.findAllActiveWithColumns();
    }
    
    public TableMetadata getTableByName(String tableName) {
        return tableMetadataRepository.findByTableName(tableName);
    }
    
    public List<ColumnMetadata> getSelectableColumns(String tableName) {
        TableMetadata table = tableMetadataRepository.findByTableName(tableName);
        if (table != null) {
            return columnMetadataRepository.findByTableAndIsSelectableTrue(table);
        }
        return List.of();
    }
    
    public List<ColumnMetadata> getFilterableColumns(String tableName) {
        TableMetadata table = tableMetadataRepository.findByTableName(tableName);
        if (table != null) {
            return columnMetadataRepository.findByTableAndIsFilterableTrue(table);
        }
        return List.of();
    }
    
    public List<JoinMetadata> getAvailableJoins(String tableName) {
        TableMetadata table = tableMetadataRepository.findByTableName(tableName);
        if (table != null) {
            return joinMetadataRepository.findByTable(table);
        }
        return List.of();
    }
    
    @Transactional
    public TableMetadata createTable(TableMetadata tableMetadata) {
        return tableMetadataRepository.save(tableMetadata);
    }
    
    @Transactional
    public ColumnMetadata createColumn(ColumnMetadata columnMetadata) {
        return columnMetadataRepository.save(columnMetadata);
    }
    
    @Transactional
    public JoinMetadata createJoin(JoinMetadata joinMetadata) {
        return joinMetadataRepository.save(joinMetadata);
    }
    
    @Transactional
    public void initializeBusinessMetadata() {
        TableMetadata fxrateTable = new TableMetadata("fxrate_info", "FX Rates", "Foreign exchange rates by month and year");
        TableMetadata scenarioTable = new TableMetadata("scenario_info", "Scenarios", "Business scenarios with date ranges");
        TableMetadata accountTable = new TableMetadata("account_info", "Accounts", "Account hierarchy information");
        TableMetadata segmentTable = new TableMetadata("segment_info", "Segments", "Business segment hierarchy");
        TableMetadata geographyTable = new TableMetadata("geography_info", "Geography", "Geographic hierarchy information");
        TableMetadata gocTable = new TableMetadata("goc_info", "GOC Information", "General Operating Company data");
        TableMetadata userTable = new TableMetadata("user_info", "Users", "System user information");
        
        fxrateTable = tableMetadataRepository.save(fxrateTable);
        scenarioTable = tableMetadataRepository.save(scenarioTable);
        accountTable = tableMetadataRepository.save(accountTable);
        segmentTable = tableMetadataRepository.save(segmentTable);
        geographyTable = tableMetadataRepository.save(geographyTable);
        gocTable = tableMetadataRepository.save(gocTable);
        userTable = tableMetadataRepository.save(userTable);
        
        columnMetadataRepository.save(new ColumnMetadata(fxrateTable, "fx_id", "FX ID", "VARCHAR"));
        columnMetadataRepository.save(new ColumnMetadata(fxrateTable, "fx_name", "FX Name", "VARCHAR"));
        columnMetadataRepository.save(new ColumnMetadata(fxrateTable, "year", "Year", "INTEGER"));
        columnMetadataRepository.save(new ColumnMetadata(fxrateTable, "currency", "Currency", "VARCHAR"));
        for (int i = 1; i <= 12; i++) {
            String monthName = getMonthName(i);
            columnMetadataRepository.save(new ColumnMetadata(fxrateTable, "m" + i + "_rate", monthName + " Rate", "DECIMAL"));
        }
        
        columnMetadataRepository.save(new ColumnMetadata(scenarioTable, "scenario_id", "Scenario ID", "BIGINT"));
        columnMetadataRepository.save(new ColumnMetadata(scenarioTable, "scenario_name", "Scenario Name", "VARCHAR"));
        columnMetadataRepository.save(new ColumnMetadata(scenarioTable, "start_date", "Start Date", "DATE"));
        columnMetadataRepository.save(new ColumnMetadata(scenarioTable, "end_date", "End Date", "DATE"));
        columnMetadataRepository.save(new ColumnMetadata(scenarioTable, "fx_rate", "FX Rate Reference", "VARCHAR"));
        
        columnMetadataRepository.save(new ColumnMetadata(accountTable, "account_id", "Account ID", "VARCHAR"));
        columnMetadataRepository.save(new ColumnMetadata(accountTable, "account_parent_id", "Parent Account ID", "VARCHAR"));
        columnMetadataRepository.save(new ColumnMetadata(accountTable, "period_id", "Period ID", "VARCHAR"));
        
        columnMetadataRepository.save(new ColumnMetadata(segmentTable, "segment_id", "Segment ID", "VARCHAR"));
        columnMetadataRepository.save(new ColumnMetadata(segmentTable, "segment_parent_id", "Parent Segment ID", "VARCHAR"));
        columnMetadataRepository.save(new ColumnMetadata(segmentTable, "period_id", "Period ID", "VARCHAR"));
        
        columnMetadataRepository.save(new ColumnMetadata(geographyTable, "geo_id", "Geography ID", "VARCHAR"));
        columnMetadataRepository.save(new ColumnMetadata(geographyTable, "geo_parent_id", "Parent Geography ID", "VARCHAR"));
        columnMetadataRepository.save(new ColumnMetadata(geographyTable, "period_id", "Period ID", "VARCHAR"));
        
        columnMetadataRepository.save(new ColumnMetadata(gocTable, "goc", "GOC", "VARCHAR"));
        columnMetadataRepository.save(new ColumnMetadata(gocTable, "segment_id", "Segment ID", "VARCHAR"));
        columnMetadataRepository.save(new ColumnMetadata(gocTable, "geo_id", "Geography ID", "VARCHAR"));
        columnMetadataRepository.save(new ColumnMetadata(gocTable, "period_id", "Period ID", "VARCHAR"));
        
        columnMetadataRepository.save(new ColumnMetadata(userTable, "user_id", "User ID", "BIGINT"));
        columnMetadataRepository.save(new ColumnMetadata(userTable, "name", "Name", "VARCHAR"));
        columnMetadataRepository.save(new ColumnMetadata(userTable, "role", "Role", "VARCHAR"));
        
        joinMetadataRepository.save(new JoinMetadata(scenarioTable, fxrateTable, "fx_rate", "fx_id", "LEFT"));
        joinMetadataRepository.save(new JoinMetadata(gocTable, segmentTable, "segment_id", "segment_id", "INNER"));
        joinMetadataRepository.save(new JoinMetadata(gocTable, geographyTable, "geo_id", "geo_id", "INNER"));
    }
    
    public List<Map<String, Object>> executeQuery(String sql) {
        return jdbcTemplate.queryForList(sql);
    }
    
    private String getMonthName(int month) {
        String[] months = {"January", "February", "March", "April", "May", "June",
                          "July", "August", "September", "October", "November", "December"};
        return months[month - 1];
    }
}
