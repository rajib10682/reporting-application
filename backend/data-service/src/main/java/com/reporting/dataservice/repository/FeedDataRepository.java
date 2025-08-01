package com.reporting.dataservice.repository;

import com.reporting.dataservice.model.FeedData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedDataRepository extends JpaRepository<FeedData, Long> {
}
