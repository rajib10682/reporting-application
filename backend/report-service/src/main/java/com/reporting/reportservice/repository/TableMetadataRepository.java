package com.reporting.reportservice.repository;

import com.reporting.reportservice.model.TableMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TableMetadataRepository extends JpaRepository<TableMetadata, Long> {
    
    List<TableMetadata> findByIsActiveTrue();
    
    TableMetadata findByTableName(String tableName);
    
    @Query("SELECT t FROM TableMetadata t LEFT JOIN FETCH t.columns WHERE t.isActive = true")
    List<TableMetadata> findAllActiveWithColumns();
}
