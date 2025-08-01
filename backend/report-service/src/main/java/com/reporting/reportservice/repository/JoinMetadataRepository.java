package com.reporting.reportservice.repository;

import com.reporting.reportservice.model.JoinMetadata;
import com.reporting.reportservice.model.TableMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JoinMetadataRepository extends JpaRepository<JoinMetadata, Long> {
    
    List<JoinMetadata> findBySourceTable(TableMetadata sourceTable);
    
    List<JoinMetadata> findByTargetTable(TableMetadata targetTable);
    
    @Query("SELECT j FROM JoinMetadata j WHERE j.sourceTable = :table OR j.targetTable = :table")
    List<JoinMetadata> findByTable(@Param("table") TableMetadata table);
    
    @Query("SELECT j FROM JoinMetadata j WHERE " +
           "(j.sourceTable.id = :sourceId AND j.targetTable.id = :targetId) OR " +
           "(j.sourceTable.id = :targetId AND j.targetTable.id = :sourceId)")
    List<JoinMetadata> findJoinsBetweenTables(@Param("sourceId") Long sourceId, @Param("targetId") Long targetId);
}
