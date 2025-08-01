package com.reporting.reportservice.repository;

import com.reporting.reportservice.model.ColumnMetadata;
import com.reporting.reportservice.model.TableMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ColumnMetadataRepository extends JpaRepository<ColumnMetadata, Long> {
    
    List<ColumnMetadata> findByTable(TableMetadata table);
    
    List<ColumnMetadata> findByTableAndIsSelectableTrue(TableMetadata table);
    
    List<ColumnMetadata> findByTableAndIsFilterableTrue(TableMetadata table);
    
    ColumnMetadata findByTableAndColumnName(TableMetadata table, String columnName);
}
