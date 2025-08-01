# Advanced Reporting Application - Design Plan

## Overview
This document outlines the design for an advanced reporting application with dynamic SQL generation, concurrency management, and sophisticated queuing capabilities.

## Architecture Overview

### High-Level Architecture
```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Angular UI    │────│   API Gateway    │────│  Report Engine  │
│                 │    │   (Port 8080)    │    │   (Port 8082)   │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │                        │
                       ┌────────┴────────┐              │
                       │                 │              │
                ┌──────▼──────┐   ┌──────▼──────┐      │
                │ User Service │   │ Data Service │      │
                │ (Port 8081) │   │ (Port 8083) │      │
                └─────────────┘   └─────────────┘      │
                                                       │
                ┌──────────────────────────────────────┘
                │
        ┌───────▼────────┐    ┌─────────────────┐
        │ Queue Manager  │    │ Metadata Store  │
        │   (Redis)      │    │   (Database)    │
        └────────────────┘    └─────────────────┘
```

## Core Components

### 1. Dynamic SQL Generation Engine

#### Components:
- **Query Builder Service**: Constructs SQL queries based on user selections
- **Metadata Manager**: Manages table relationships and column metadata
- **Join Optimizer**: Optimizes table joins based on metadata

#### Key Features:
- Dynamic WHERE clause generation based on user filters
- Automatic JOIN detection using metadata tables
- Column selection and ordering
- Query validation and optimization

#### Database Schema for Metadata:
```sql
-- Table metadata
CREATE TABLE table_metadata (
    id BIGINT PRIMARY KEY,
    table_name VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Column metadata
CREATE TABLE column_metadata (
    id BIGINT PRIMARY KEY,
    table_id BIGINT REFERENCES table_metadata(id),
    column_name VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    data_type VARCHAR(50),
    is_filterable BOOLEAN DEFAULT true,
    is_selectable BOOLEAN DEFAULT true,
    description TEXT
);

-- Join relationships
CREATE TABLE join_metadata (
    id BIGINT PRIMARY KEY,
    source_table_id BIGINT REFERENCES table_metadata(id),
    target_table_id BIGINT REFERENCES table_metadata(id),
    source_column VARCHAR(255),
    target_column VARCHAR(255),
    join_type VARCHAR(20) DEFAULT 'INNER',
    description TEXT
);
```

### 2. Report Management System

#### Report Status Lifecycle:
```
PENDING → QUEUED → EXECUTING → COMPLETED
                      ↓
                   FAILED
```

#### Database Schema:
```sql
-- Report requests
CREATE TABLE report_requests (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    report_name VARCHAR(255),
    query_config TEXT, -- JSON configuration
    status VARCHAR(20) DEFAULT 'PENDING',
    priority INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    error_message TEXT,
    result_file_path VARCHAR(500)
);

-- Report templates
CREATE TABLE report_templates (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    template_name VARCHAR(255) NOT NULL,
    description TEXT,
    query_config TEXT, -- JSON configuration
    is_public BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- System configuration
CREATE TABLE system_config (
    config_key VARCHAR(100) PRIMARY KEY,
    config_value VARCHAR(500),
    description TEXT
);
```

### 3. Concurrency Management & Queuing System

#### Queue Manager Features:
- **Concurrent Execution Limit**: Configurable system-wide limit
- **User-based Prioritization**: Prevents single user monopolization
- **Queue Management**: FIFO with priority adjustments
- **Status Tracking**: Real-time status updates

#### Queue Algorithm:
```
1. Check if concurrent limit reached
2. If limit reached:
   - Add to queue
   - Apply user prioritization rules
3. If under limit:
   - Start execution immediately
4. On completion:
   - Process next item in queue
   - Apply prioritization rules
```

#### Priority Rules:
- New users get higher priority than users with running reports
- Within same priority level: FIFO
- System admin reports get highest priority

### 4. Frontend Architecture

#### New Components:
```
src/app/
├── components/
│   ├── report-builder/
│   │   ├── table-selector/
│   │   ├── column-selector/
│   │   ├── filter-builder/
│   │   └── query-preview/
│   ├── report-queue/
│   ├── report-templates/
│   └── report-results/
├── services/
│   ├── query-builder.service.ts
│   ├── metadata.service.ts
│   ├── queue.service.ts
│   └── template.service.ts
└── models/
    ├── report-config.model.ts
    ├── metadata.model.ts
    └── queue-item.model.ts
```

#### UI Flow:
1. **Report Builder**: Drag-and-drop interface for table/column selection
2. **Filter Builder**: Dynamic filter creation with operators
3. **Preview**: SQL query preview before execution
4. **Queue Monitor**: Real-time queue status and position
5. **Results**: Downloadable results with export options

### 5. Backend Services Enhancement

#### Report Service (Enhanced):
```java
@RestController
@RequestMapping("/api/reports")
public class ReportController {
    
    @PostMapping("/build")
    public ResponseEntity<ReportRequest> buildReport(@RequestBody ReportConfig config);
    
    @GetMapping("/queue/status")
    public ResponseEntity<QueueStatus> getQueueStatus();
    
    @GetMapping("/templates")
    public ResponseEntity<List<ReportTemplate>> getTemplates();
    
    @PostMapping("/templates")
    public ResponseEntity<ReportTemplate> saveTemplate(@RequestBody ReportTemplate template);
}
```

#### New Services:
- **QueryBuilderService**: Dynamic SQL generation
- **QueueManagerService**: Concurrency and queue management
- **MetadataService**: Table/column metadata management
- **ExecutionService**: Report execution with status tracking

### 6. Technology Stack Enhancements

#### Additional Dependencies:
- **Backend**:
  - Redis for queue management
  - Apache Commons CSV for export
  - Quartz Scheduler for background processing
  - HikariCP for connection pooling

- **Frontend**:
  - Angular CDK for drag-and-drop
  - ngx-charts for advanced visualizations
  - Socket.IO for real-time updates
  - File-saver for downloads

## Implementation Phases

### Phase 1: Core Infrastructure
1. Database schema setup
2. Metadata management system
3. Basic query builder service
4. Queue management foundation

### Phase 2: Dynamic Query Generation
1. SQL builder implementation
2. Join optimization
3. Filter system
4. Query validation

### Phase 3: Concurrency & Queue Management
1. Redis integration
2. Queue processing logic
3. Priority management
4. Status tracking

### Phase 4: Frontend Enhancement
1. Report builder UI
2. Queue monitoring
3. Template management
4. Real-time updates

### Phase 5: Advanced Features
1. Export functionality
2. Scheduled reports
3. Performance optimization
4. Security enhancements

## Configuration Management

### System Configuration:
```yaml
reporting:
  concurrency:
    max-concurrent-reports: 5
    queue-timeout-minutes: 30
  database:
    query-timeout-seconds: 300
    max-result-rows: 100000
  export:
    formats: [CSV, EXCEL, PDF]
    max-file-size-mb: 50
```

## Security Considerations

1. **SQL Injection Prevention**: Parameterized queries only
2. **Access Control**: Role-based table/column access
3. **Resource Limits**: Query timeout and result size limits
4. **Audit Trail**: Complete audit log of all report activities

## Performance Optimization

1. **Query Caching**: Cache frequently used queries
2. **Result Pagination**: Large result set handling
3. **Connection Pooling**: Optimized database connections
4. **Async Processing**: Non-blocking report execution

## Monitoring & Alerting

1. **Queue Metrics**: Queue length, processing time
2. **System Health**: Database connections, memory usage
3. **User Activity**: Report generation patterns
4. **Error Tracking**: Failed reports and error analysis

This design provides a robust foundation for building an enterprise-grade reporting application with advanced features for dynamic report generation, concurrency management, and user interaction.
