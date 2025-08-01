package com.reporting.reportservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "join_metadata")
public class JoinMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_table_id", nullable = false)
    private TableMetadata sourceTable;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_table_id", nullable = false)
    private TableMetadata targetTable;
    
    @Column(name = "source_column")
    private String sourceColumn;
    
    @Column(name = "target_column")
    private String targetColumn;
    
    @Column(name = "join_type")
    private String joinType = "INNER";
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    public JoinMetadata() {}
    
    public JoinMetadata(TableMetadata sourceTable, TableMetadata targetTable, 
                       String sourceColumn, String targetColumn, String joinType) {
        this.sourceTable = sourceTable;
        this.targetTable = targetTable;
        this.sourceColumn = sourceColumn;
        this.targetColumn = targetColumn;
        this.joinType = joinType;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public TableMetadata getSourceTable() { return sourceTable; }
    public void setSourceTable(TableMetadata sourceTable) { this.sourceTable = sourceTable; }
    
    public TableMetadata getTargetTable() { return targetTable; }
    public void setTargetTable(TableMetadata targetTable) { this.targetTable = targetTable; }
    
    public String getSourceColumn() { return sourceColumn; }
    public void setSourceColumn(String sourceColumn) { this.sourceColumn = sourceColumn; }
    
    public String getTargetColumn() { return targetColumn; }
    public void setTargetColumn(String targetColumn) { this.targetColumn = targetColumn; }
    
    public String getJoinType() { return joinType; }
    public void setJoinType(String joinType) { this.joinType = joinType; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
