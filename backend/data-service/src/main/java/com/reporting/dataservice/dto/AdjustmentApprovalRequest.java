package com.reporting.dataservice.dto;

public class AdjustmentApprovalRequest {
    private Long sessionId;
    private String action; // APPROVE or REJECT
    private Long approvedBy;
    private String comments;

    public AdjustmentApprovalRequest() {}

    public AdjustmentApprovalRequest(Long sessionId, String action, Long approvedBy, String comments) {
        this.sessionId = sessionId;
        this.action = action;
        this.approvedBy = approvedBy;
        this.comments = comments;
    }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Long getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Long approvedBy) { this.approvedBy = approvedBy; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}
