package com.reporting.reportservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "report_data")
public class ReportData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "report_id")
    private Long reportId;
    
    private String category;
    private Double value;
    private String label;
    private String period;

    public ReportData() {}

    public ReportData(Long reportId, String category, Double value, String label, String period) {
        this.reportId = reportId;
        this.category = category;
        this.value = value;
        this.label = label;
        this.period = period;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
}
