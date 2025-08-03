package com.reporting.dataservice.dto;

public class AdjustmentUploadRequest {
    private Long scenarioId;
    private String uploadType; // INCREMENTAL or REPLACE
    private Long submittedBy;
    private String filename;

    public AdjustmentUploadRequest() {}

    public AdjustmentUploadRequest(Long scenarioId, String uploadType, Long submittedBy, String filename) {
        this.scenarioId = scenarioId;
        this.uploadType = uploadType;
        this.submittedBy = submittedBy;
        this.filename = filename;
    }

    public Long getScenarioId() { return scenarioId; }
    public void setScenarioId(Long scenarioId) { this.scenarioId = scenarioId; }

    public String getUploadType() { return uploadType; }
    public void setUploadType(String uploadType) { this.uploadType = uploadType; }

    public Long getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(Long submittedBy) { this.submittedBy = submittedBy; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
}
