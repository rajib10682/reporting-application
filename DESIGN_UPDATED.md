# Advanced Reporting Application - Updated Design Plan

## Overview
This document outlines the updated design for an advanced reporting application with dynamic SQL generation, incorporating the user's specific metadata tables: fxrate_info, scenario_info, account_info, segment_info, geography_info, goc_info, and user_info.

## Business Data Model Analysis

### User-Provided Tables and Relationships

#### 1. FX Rate Information (fxrate_info)
- **Composite Key**: fx_id, fx_name, year, currency
- **Monthly Rates**: m1_rate through m12_rate (DECIMAL)
- **Purpose**: Foreign exchange rates by month and year
- **Usage**: Referenced by scenario_info for financial calculations

#### 2. Scenario Information (scenario_info)
- **Primary Key**: scenario_id
- **Unique Constraint**: scenario_name
- **Foreign Key**: fx_rate → fxrate_info.fx_id (many-to-one)
- **Purpose**: Business scenarios with date ranges and FX rate associations

#### 3. Hierarchical Data Tables
All following tables use composite keys and represent hierarchical structures:

- **account_info**: account_id, account_parent_id, period_id
- **segment_info**: segment_id, segment_parent_id, period_id  
- **geography_info**: geo_id, geo_parent_id, period_id

#### 4. GOC Information (goc_info)
- **Composite Key**: goc, segment_id, geo_id, period_id
- **Foreign Keys**: 
  - segment_id → segment_info.segment_id (many-to-one)
  - geo_id → geography_info.geo_id (many-to-one)
- **Purpose**: Central data linking geography and segments

#### 5. User Information (user_info)
- **Primary Key**: user_id
- **Purpose**: System user management with roles

## Enhanced Architecture for Business Data

### Dynamic Query Builder Enhancements

#### Composite Key Handling
```java
public class CompositeKeyHandler {
    public String buildCompositeKeyJoin(TableMetadata sourceTable, TableMetadata targetTable) {
        // Handle complex joins for composite keys
        // Example: goc_info.segment_id = segment_info.segment_id 
        //          AND goc_info.period_id = segment_info.period_id
    }
}
```

#### Hierarchical Data Support
```java
public class HierarchyQueryBuilder {
    public String buildHierarchicalQuery(String tableName, int levels) {
        // Generate recursive CTEs for parent-child relationships
        // Support account/segment/geography hierarchies
    }
}
```

### Metadata Initialization for Business Tables

```sql
-- Business tables metadata
INSERT INTO table_metadata (table_name, display_name, description, is_active) VALUES
('fxrate_info', 'FX Rates', 'Foreign exchange rates by month and year', true),
('scenario_info', 'Scenarios', 'Business scenarios with date ranges', true),
('account_info', 'Accounts', 'Account hierarchy information', true),
('segment_info', 'Segments', 'Business segment hierarchy', true),
('geography_info', 'Geography', 'Geographic hierarchy information', true),
('goc_info', 'GOC Information', 'General Operating Company data', true),
('user_info', 'Users', 'System user information', true);

-- FX Rate columns
INSERT INTO column_metadata (table_id, column_name, display_name, data_type, is_filterable, is_selectable) VALUES
(1, 'fx_id', 'FX ID', 'VARCHAR', true, true),
(1, 'fx_name', 'FX Name', 'VARCHAR', true, true),
(1, 'year', 'Year', 'INTEGER', true, true),
(1, 'currency', 'Currency', 'VARCHAR', true, true),
(1, 'm1_rate', 'January Rate', 'DECIMAL', false, true),
-- ... (m2_rate through m12_rate)
(1, 'm12_rate', 'December Rate', 'DECIMAL', false, true);

-- Relationship definitions
INSERT INTO join_metadata (source_table_id, target_table_id, source_column, target_column, join_type) VALUES
(2, 1, 'fx_rate', 'fx_id', 'LEFT'),           -- scenario_info -> fxrate_info
(6, 4, 'segment_id', 'segment_id', 'INNER'),  -- goc_info -> segment_info
(6, 5, 'geo_id', 'geo_id', 'INNER');          -- goc_info -> geography_info
```

## Enhanced Use Cases with Real Data

### Use Case 1: FX Rate Analysis by Scenario
```sql
-- Generated query example
SELECT 
    s.scenario_name,
    s.start_date,
    s.end_date,
    f.currency,
    f.year,
    f.m1_rate as "January Rate",
    f.m6_rate as "June Rate",
    f.m12_rate as "December Rate"
FROM scenario_info s
LEFT JOIN fxrate_info f ON s.fx_rate = f.fx_id
WHERE f.year = 2024 
  AND f.currency IN ('USD', 'EUR')
ORDER BY s.scenario_name, f.currency;
```

### Use Case 2: GOC Performance by Geography and Segment
```sql
-- Complex join with composite keys
SELECT 
    g.goc,
    s.segment_id,
    s.segment_parent_id,
    geo.geo_id,
    geo.geo_parent_id,
    g.period_id
FROM goc_info g
INNER JOIN segment_info s ON (g.segment_id = s.segment_id AND g.period_id = s.period_id)
INNER JOIN geography_info geo ON (g.geo_id = geo.geo_id AND g.period_id = geo.period_id)
WHERE g.period_id = '2024Q1';
```

### Use Case 3: Hierarchical Account Analysis
```sql
-- Recursive hierarchy query
WITH RECURSIVE account_hierarchy AS (
    -- Base case: top-level accounts
    SELECT account_id, account_parent_id, period_id, 0 as level
    FROM account_info 
    WHERE account_parent_id = 'ROOT'
    
    UNION ALL
    
    -- Recursive case
    SELECT a.account_id, a.account_parent_id, a.period_id, ah.level + 1
    FROM account_info a
    INNER JOIN account_hierarchy ah ON a.account_parent_id = ah.account_id
)
SELECT * FROM account_hierarchy WHERE period_id = '2024Q1';
```

## Frontend Enhancements for Business Data

### Enhanced Report Builder Component
```typescript
export interface BusinessTableConfig {
  fxrateInfo: {
    compositeKey: ['fx_id', 'fx_name', 'year', 'currency'];
    monthlyRates: string[]; // m1_rate through m12_rate
  };
  hierarchicalTables: {
    accountInfo: { keyFields: ['account_id', 'account_parent_id', 'period_id'] };
    segmentInfo: { keyFields: ['segment_id', 'segment_parent_id', 'period_id'] };
    geographyInfo: { keyFields: ['geo_id', 'geo_parent_id', 'period_id'] };
  };
  gocInfo: {
    compositeKey: ['goc', 'segment_id', 'geo_id', 'period_id'];
    foreignKeys: {
      segment_id: 'segment_info.segment_id';
      geo_id: 'geography_info.geo_id';
    };
  };
}
```

### Specialized UI Components
1. **FX Rate Selector**: Multi-dimensional picker for fx_id, year, currency
2. **Hierarchy Browser**: Tree view for account/segment/geography hierarchies
3. **Period Filter**: Specialized period_id filtering across tables
4. **Composite Key Handler**: UI for complex key relationships

## API Enhancements for Business Data

### Metadata Endpoints
```java
@RestController
@RequestMapping("/api/metadata")
public class BusinessMetadataController {
    
    @GetMapping("/fxrates/currencies")
    public List<String> getAvailableCurrencies();
    
    @GetMapping("/fxrates/years")
    public List<Integer> getAvailableYears();
    
    @GetMapping("/hierarchies/{tableName}")
    public HierarchyNode getHierarchy(@PathVariable String tableName, 
                                     @RequestParam String periodId);
    
    @GetMapping("/periods")
    public List<String> getAvailablePeriods();
    
    @PostMapping("/validate-composite-join")
    public ValidationResult validateCompositeJoin(@RequestBody JoinRequest request);
}
```

### Enhanced Query Builder
```java
@Service
public class BusinessQueryBuilderService extends QueryBuilderService {
    
    public String buildFXRateQuery(FXRateQueryConfig config) {
        // Handle composite key joins for FX rates
        // Support monthly rate aggregations
    }
    
    public String buildHierarchicalQuery(HierarchyQueryConfig config) {
        // Generate recursive CTEs for hierarchical data
        // Handle parent-child relationships
    }
    
    public String buildGOCAnalysisQuery(GOCQueryConfig config) {
        // Complex joins across goc_info, segment_info, geography_info
        // Handle multiple composite key relationships
    }
}
```

## Configuration for Business Data

### Enhanced Application Configuration
```yaml
reporting:
  business-data:
    fx-rates:
      default-currency: "USD"
      supported-currencies: ["USD", "EUR", "GBP", "JPY"]
      monthly-rate-columns: ["m1_rate", "m2_rate", ..., "m12_rate"]
    
    hierarchies:
      max-depth: 10
      default-period: "2024Q1"
      supported-periods: ["2024Q1", "2024Q2", "2024Q3", "2024Q4"]
    
    composite-keys:
      validation-enabled: true
      auto-join-detection: true
      
  query-optimization:
    composite-key-indexing: true
    hierarchy-caching: true
    period-partitioning: true
```

## Performance Considerations for Business Data

### Indexing Strategy
```sql
-- Composite key indexes
CREATE INDEX idx_fxrate_composite ON fxrate_info(fx_id, fx_name, year, currency);
CREATE INDEX idx_goc_composite ON goc_info(goc, segment_id, geo_id, period_id);

-- Foreign key indexes
CREATE INDEX idx_scenario_fx_rate ON scenario_info(fx_rate);
CREATE INDEX idx_goc_segment ON goc_info(segment_id, period_id);
CREATE INDEX idx_goc_geo ON goc_info(geo_id, period_id);

-- Period-based partitioning
CREATE INDEX idx_period_account ON account_info(period_id);
CREATE INDEX idx_period_segment ON segment_info(period_id);
CREATE INDEX idx_period_geography ON geography_info(period_id);
```

### Query Optimization
1. **Composite Key Optimization**: Efficient handling of multi-column keys
2. **Hierarchy Caching**: Cache hierarchical structures by period
3. **Period Partitioning**: Partition large tables by period_id
4. **FX Rate Aggregation**: Pre-calculate common FX rate aggregations

## Integration with Existing Codebase

### Migration Strategy
1. **Phase 1**: Update metadata tables with business schema
2. **Phase 2**: Enhance QueryBuilderService for composite keys
3. **Phase 3**: Add specialized UI components for business data
4. **Phase 4**: Implement hierarchy and FX rate specific features
5. **Phase 5**: Performance optimization and testing

### Backward Compatibility
- Maintain existing report service APIs
- Gradual migration of metadata initialization
- Support both generic and business-specific query patterns

This updated design specifically addresses the user's business data model while maintaining the advanced reporting capabilities for dynamic SQL generation, concurrency management, and sophisticated queuing.
