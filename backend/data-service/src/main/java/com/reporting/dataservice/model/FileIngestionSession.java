package com.reporting.dataservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.Duration;

@Entity
@Table(name = "file_ingestion_session")
public class FileIngestionSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long sessionId;
    
    @Column(name = "filename", nullable = false, length = 255)
    private String filename;
    
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
    
    @Column(name = "status", length = 50)
    private String status;
    
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    public FileIngestionSession() {
        this.createdAt = LocalDateTime.now();
        this.startTime = LocalDateTime.now();
    }
    
    public FileIngestionSession(String filename) {
        this();
        this.filename = filename;
    }
    
    public void markCompleted() {
        this.endTime = LocalDateTime.now();
        if (this.startTime != null) {
            this.durationMs = Duration.between(this.startTime, this.endTime).toMillis();
        }
    }
    
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    
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
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
