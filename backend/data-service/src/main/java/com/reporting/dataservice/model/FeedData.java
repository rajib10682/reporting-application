package com.reporting.dataservice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "feed_data")
public class FeedData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;
    
    @Column(name = "ingestion_session_id", nullable = false)
    private Long ingestionSessionId;
    
    @Column(name = "scenario_id", nullable = false)
    private Long scenarioId;
    
    @Column(name = "goc", nullable = false, length = 50)
    private String goc;
    
    @Column(name = "account", nullable = false, length = 50)
    private String account;
    
    @Column(name = "fiscal_year", nullable = false)
    private Integer fiscalYear;
    
    @Column(name = "currency", nullable = false, length = 10)
    private String currency;
    
    @Column(name = "jan_amt", precision = 15, scale = 2)
    private BigDecimal janAmt;
    
    @Column(name = "feb_amt", precision = 15, scale = 2)
    private BigDecimal febAmt;
    
    @Column(name = "mar_amt", precision = 15, scale = 2)
    private BigDecimal marAmt;
    
    @Column(name = "apr_amt", precision = 15, scale = 2)
    private BigDecimal aprAmt;
    
    @Column(name = "may_amt", precision = 15, scale = 2)
    private BigDecimal mayAmt;
    
    @Column(name = "jun_amt", precision = 15, scale = 2)
    private BigDecimal junAmt;
    
    @Column(name = "jul_amt", precision = 15, scale = 2)
    private BigDecimal julAmt;
    
    @Column(name = "aug_amt", precision = 15, scale = 2)
    private BigDecimal augAmt;
    
    @Column(name = "sep_amt", precision = 15, scale = 2)
    private BigDecimal sepAmt;
    
    @Column(name = "oct_amt", precision = 15, scale = 2)
    private BigDecimal octAmt;
    
    @Column(name = "nov_amt", precision = 15, scale = 2)
    private BigDecimal novAmt;
    
    @Column(name = "dec_amt", precision = 15, scale = 2)
    private BigDecimal decAmt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    public FeedData() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    
    public Long getIngestionSessionId() { return ingestionSessionId; }
    public void setIngestionSessionId(Long ingestionSessionId) { this.ingestionSessionId = ingestionSessionId; }
    
    public Long getScenarioId() { return scenarioId; }
    public void setScenarioId(Long scenarioId) { this.scenarioId = scenarioId; }
    
    public String getGoc() { return goc; }
    public void setGoc(String goc) { this.goc = goc; }
    
    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }
    
    public Integer getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(Integer fiscalYear) { this.fiscalYear = fiscalYear; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public BigDecimal getJanAmt() { return janAmt; }
    public void setJanAmt(BigDecimal janAmt) { this.janAmt = janAmt; }
    
    public BigDecimal getFebAmt() { return febAmt; }
    public void setFebAmt(BigDecimal febAmt) { this.febAmt = febAmt; }
    
    public BigDecimal getMarAmt() { return marAmt; }
    public void setMarAmt(BigDecimal marAmt) { this.marAmt = marAmt; }
    
    public BigDecimal getAprAmt() { return aprAmt; }
    public void setAprAmt(BigDecimal aprAmt) { this.aprAmt = aprAmt; }
    
    public BigDecimal getMayAmt() { return mayAmt; }
    public void setMayAmt(BigDecimal mayAmt) { this.mayAmt = mayAmt; }
    
    public BigDecimal getJunAmt() { return junAmt; }
    public void setJunAmt(BigDecimal junAmt) { this.junAmt = junAmt; }
    
    public BigDecimal getJulAmt() { return julAmt; }
    public void setJulAmt(BigDecimal julAmt) { this.julAmt = julAmt; }
    
    public BigDecimal getAugAmt() { return augAmt; }
    public void setAugAmt(BigDecimal augAmt) { this.augAmt = augAmt; }
    
    public BigDecimal getSepAmt() { return sepAmt; }
    public void setSepAmt(BigDecimal sepAmt) { this.sepAmt = sepAmt; }
    
    public BigDecimal getOctAmt() { return octAmt; }
    public void setOctAmt(BigDecimal octAmt) { this.octAmt = octAmt; }
    
    public BigDecimal getNovAmt() { return novAmt; }
    public void setNovAmt(BigDecimal novAmt) { this.novAmt = novAmt; }
    
    public BigDecimal getDecAmt() { return decAmt; }
    public void setDecAmt(BigDecimal decAmt) { this.decAmt = decAmt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
