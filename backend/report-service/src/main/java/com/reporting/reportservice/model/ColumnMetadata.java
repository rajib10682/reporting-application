package com.reporting.reportservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "column_metadata")
public class ColumnMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private TableMetadata table;
    
    @Column(name = "column_name", nullable = false)
    private String columnName;
    
    @Column(name = "display_name")
    private String displayName;
    
    @Column(name = "data_type")
    private String dataType;
    
    @Column(name = "is_filterable")
    private Boolean isFilterable = true;
    
    @Column(name = "is_selectable")
    private Boolean isSelectable = true;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    public ColumnMetadata() {}
    
    public ColumnMetadata(TableMetadata table, String columnName, String displayName, String dataType) {
        this.table = table;
        this.columnName = columnName;
        this.displayName = displayName;
        this.dataType = dataType;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public TableMetadata getTable() { return table; }
    public void setTable(TableMetadata table) { this.table = table; }
    
    public String getColumnName() { return columnName; }
    public void setColumnName(String columnName) { this.columnName = columnName; }
    
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    
    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }
    
    public Boolean getIsFilterable() { return isFilterable; }
    public void setIsFilterable(Boolean isFilterable) { this.isFilterable = isFilterable; }
    
    public Boolean getIsSelectable() { return isSelectable; }
    public void setIsSelectable(Boolean isSelectable) { this.isSelectable = isSelectable; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
