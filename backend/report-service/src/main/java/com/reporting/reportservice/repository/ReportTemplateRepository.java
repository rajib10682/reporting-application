package com.reporting.reportservice.repository;

import com.reporting.reportservice.model.ReportTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportTemplateRepository extends JpaRepository<ReportTemplate, Long> {
    
    List<ReportTemplate> findByUserId(Long userId);
    
    List<ReportTemplate> findByIsPublicTrue();
    
    List<ReportTemplate> findByUserIdOrIsPublicTrue(Long userId, Boolean isPublic);
    
    ReportTemplate findByUserIdAndTemplateName(Long userId, String templateName);
}
