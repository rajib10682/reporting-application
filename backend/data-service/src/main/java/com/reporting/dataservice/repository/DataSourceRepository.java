package com.reporting.dataservice.repository;

import com.reporting.dataservice.model.DataSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataSourceRepository extends JpaRepository<DataSource, Long> {
}
