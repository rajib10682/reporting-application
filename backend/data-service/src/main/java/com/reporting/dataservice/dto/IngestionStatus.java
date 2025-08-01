package com.reporting.dataservice.dto;

public class IngestionStatus {
    private String status;
    private int totalRecords;
    private int processedRecords;
    private int successfulRecords;
    private int failedRecords;
    private String message;
    
    public IngestionStatus() {}
    
    public IngestionStatus(String status, String message) {
        this.status = status;
        this.message = message;
    }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public int getTotalRecords() { return totalRecords; }
    public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }
    
    public int getProcessedRecords() { return processedRecords; }
    public void setProcessedRecords(int processedRecords) { this.processedRecords = processedRecords; }
    
    public int getSuccessfulRecords() { return successfulRecords; }
    public void setSuccessfulRecords(int successfulRecords) { this.successfulRecords = successfulRecords; }
    
    public int getFailedRecords() { return failedRecords; }
    public void setFailedRecords(int failedRecords) { this.failedRecords = failedRecords; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
