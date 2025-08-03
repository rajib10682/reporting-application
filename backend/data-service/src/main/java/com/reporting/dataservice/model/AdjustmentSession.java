package com.reporting.dataservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "adjustment_session")
public class AdjustmentSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;
    
    @Column(nullable = false)
    private String filename;
    
    @Column(name = "scenario_id", nullable = false)
    private Long scenarioId;
    
    @Column(name = "upload_type", nullable = false)
    private String uploadType; // INCREMENTAL or REPLACE
    
    @Column(name = "submitted_by", nullable = false)
    private Long submittedBy;
    
    @Column(name = "approved_by")
    private Long approvedBy;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "duration_ms")
    private Long durationMs;
    
    @Column(name = "total_records")
    private Integer totalRecords;
    
    @Column(name = "successful_records")
    private Integer successfulRecords;
    
    @Column(name = "failed_records")
    private Integer failedRecords;
    
    @Column(nullable = false)
    private String status; // PENDING, APPROVED, REJECTED, PROCESSING, COMPLETED
    
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    @Column(name = "approval_comments", length = 1000)
    private String approvalComments;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    public AdjustmentSession() {
        this.createdAt = LocalDateTime.now();
        this.status = "PENDING";
    }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public Long getScenarioId() { return scenarioId; }
    public void setScenarioId(Long scenarioId) { this.scenarioId = scenarioId; }

    public String getUploadType() { return uploadType; }
    public void setUploadType(String uploadType) { this.uploadType = uploadType; }

    public Long getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(Long submittedBy) { this.submittedBy = submittedBy; }

    public Long getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Long approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }

    public Integer getTotalRecords() { return totalRecords; }
    public void setTotalRecords(Integer totalRecords) { this.totalRecords = totalRecords; }

    public Integer getSuccessfulRecords() { return successfulRecords; }
    public void setSuccessfulRecords(Integer successfulRecords) { this.successfulRecords = successfulRecords; }

    public Integer getFailedRecords() { return failedRecords; }
    public void setFailedRecords(Integer failedRecords) { this.failedRecords = failedRecords; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getApprovalComments() { return approvalComments; }
    public void setApprovalComments(String approvalComments) { this.approvalComments = approvalComments; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
}
