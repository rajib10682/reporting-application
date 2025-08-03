package com.reporting.dataservice.service;

import com.reporting.dataservice.dto.AdjustmentUploadRequest;
import com.reporting.dataservice.dto.AdjustmentApprovalRequest;
import com.reporting.dataservice.dto.IngestionStatus;
import com.reporting.dataservice.dto.ValidationResult;
import com.reporting.dataservice.exception.ValidationException;
import com.reporting.dataservice.model.AdjustmentData;
import com.reporting.dataservice.model.AdjustmentSession;
import com.reporting.dataservice.repository.AdjustmentDataRepository;
import com.reporting.dataservice.repository.AdjustmentSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class AdjustmentIngestionService {
    
    @Autowired
    private AdjustmentDataRepository adjustmentDataRepository;
    
    @Autowired
    private AdjustmentSessionRepository adjustmentSessionRepository;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private static final List<String> VALID_CURRENCIES = Arrays.asList("USD", "EUR", "GBP", "JPY", "CAD", "AUD");
    
    public IngestionStatus processAdjustmentFile(MultipartFile file, AdjustmentUploadRequest request) {
        if (!validateSubmitterRole(request.getSubmittedBy())) {
            throw new ValidationException("User does not have submitter role");
        }
        
        AdjustmentSession session = new AdjustmentSession();
        session.setFilename(file.getOriginalFilename());
        session.setScenarioId(request.getScenarioId());
        session.setUploadType(request.getUploadType());
        session.setSubmittedBy(request.getSubmittedBy());
        session.setStartTime(LocalDateTime.now());
        session.setStatus("PENDING");
        session = adjustmentSessionRepository.save(session);
        
        IngestionStatus status = new IngestionStatus();
        status.setSessionId(session.getSessionId());
        status.setFilename(session.getFilename());
        status.setStartTime(session.getStartTime());
        status.setStatus("PENDING");
        
        List<AdjustmentData> adjustmentDataList = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                try {
                    AdjustmentData adjustmentData = parseAdjustmentLine(line, lineNumber, request.getScenarioId());
                    adjustmentData.setAdjustmentSessionId(session.getSessionId());
                    ValidationResult validation = validateAdjustmentData(adjustmentData);
                    
                    if (validation.isValid()) {
                        adjustmentDataList.add(adjustmentData);
                    } else {
                        final int currentLineNumber = lineNumber;
                        errors.addAll(validation.getErrors().stream()
                            .map(error -> "Line " + currentLineNumber + ": " + error)
                            .toList());
                    }
                } catch (Exception e) {
                    final int currentLineNumber = lineNumber;
                    errors.add("Line " + currentLineNumber + ": " + e.getMessage());
                }
            }
            
            session.setTotalRecords(lineNumber);
            session.setSuccessfulRecords(adjustmentDataList.size());
            session.setFailedRecords(errors.size());
            
            if (!adjustmentDataList.isEmpty()) {
                adjustmentDataRepository.saveAll(adjustmentDataList);
            }
            
            if (errors.isEmpty()) {
                session.setErrorMessage("All records processed successfully - awaiting approval");
                status.setMessage("All records processed successfully - awaiting approval");
            } else {
                String errorMessage = "Processed with " + errors.size() + " errors: " + String.join("; ", errors);
                session.setErrorMessage(errorMessage);
                status.setMessage(errorMessage);
            }
            
        } catch (IOException e) {
            session.setStatus("FAILED");
            session.setErrorMessage("Failed to read file: " + e.getMessage());
            status.setStatus("FAILED");
            status.setMessage("Failed to read file: " + e.getMessage());
        } finally {
            session.setEndTime(LocalDateTime.now());
            session.setDurationMs(java.time.Duration.between(session.getStartTime(), session.getEndTime()).toMillis());
            session = adjustmentSessionRepository.save(session);
            
            status.setTotalRecords(session.getTotalRecords());
            status.setSuccessfulRecords(session.getSuccessfulRecords());
            status.setFailedRecords(session.getFailedRecords());
            status.setEndTime(session.getEndTime());
            status.setDurationMs(session.getDurationMs());
        }
        
        return status;
    }
    
    public IngestionStatus processApproval(AdjustmentApprovalRequest request) {
        if (!validateApproverRole(request.getApprovedBy())) {
            throw new ValidationException("User does not have approver role");
        }
        
        AdjustmentSession session = adjustmentSessionRepository.findById(request.getSessionId())
            .orElseThrow(() -> new ValidationException("Adjustment session not found"));
        
        if (!"PENDING".equals(session.getStatus())) {
            throw new ValidationException("Adjustment session is not in pending status");
        }
        
        session.setApprovedBy(request.getApprovedBy());
        session.setApprovalComments(request.getComments());
        session.setApprovedAt(LocalDateTime.now());
        
        if ("APPROVE".equals(request.getAction())) {
            session.setStatus("APPROVED");
            
            if ("REPLACE".equals(session.getUploadType())) {
                jdbcTemplate.update("DELETE FROM feed_data WHERE scenario_id = ?", session.getScenarioId());
            }
            
            copyAdjustmentDataToFeedData(session.getSessionId());
            
        } else if ("REJECT".equals(request.getAction())) {
            session.setStatus("REJECTED");
            adjustmentDataRepository.deleteByAdjustmentSessionId(session.getSessionId());
        }
        
        session = adjustmentSessionRepository.save(session);
        
        IngestionStatus status = new IngestionStatus();
        status.setSessionId(session.getSessionId());
        status.setFilename(session.getFilename());
        status.setStatus(session.getStatus());
        status.setMessage("Adjustment " + request.getAction().toLowerCase() + "d successfully");
        status.setTotalRecords(session.getTotalRecords());
        status.setSuccessfulRecords(session.getSuccessfulRecords());
        status.setFailedRecords(session.getFailedRecords());
        
        return status;
    }
    
    public List<AdjustmentSession> getPendingAdjustments() {
        return adjustmentSessionRepository.findByStatusOrderByCreatedAtDesc("PENDING");
    }
    
    public List<AdjustmentSession> getUserAdjustments(Long userId) {
        return adjustmentSessionRepository.findBySubmittedByOrderByCreatedAtDesc(userId);
    }
    
    public List<AdjustmentSession> getAllAdjustments() {
        return adjustmentSessionRepository.findAllByOrderByCreatedAtDesc();
    }
    
    private AdjustmentData parseAdjustmentLine(String line, int lineNumber, Long scenarioId) {
        String[] parts = line.split("~");
        
        if (parts.length != 16) {
            throw new ValidationException("Invalid format. Expected 16 fields, got " + parts.length);
        }
        
        AdjustmentData adjustmentData = new AdjustmentData();
        
        try {
            adjustmentData.setScenarioId(scenarioId);
            adjustmentData.setGoc(parts[0].trim());
            adjustmentData.setAccount(parts[1].trim());
            adjustmentData.setCurrency(parts[2].trim());
            adjustmentData.setFiscalYear(Integer.parseInt(parts[3].trim()));
            
            adjustmentData.setJanAmt(new BigDecimal(parts[4].trim()));
            adjustmentData.setFebAmt(new BigDecimal(parts[5].trim()));
            adjustmentData.setMarAmt(new BigDecimal(parts[6].trim()));
            adjustmentData.setAprAmt(new BigDecimal(parts[7].trim()));
            adjustmentData.setMayAmt(new BigDecimal(parts[8].trim()));
            adjustmentData.setJunAmt(new BigDecimal(parts[9].trim()));
            adjustmentData.setJulAmt(new BigDecimal(parts[10].trim()));
            adjustmentData.setAugAmt(new BigDecimal(parts[11].trim()));
            adjustmentData.setSepAmt(new BigDecimal(parts[12].trim()));
            adjustmentData.setOctAmt(new BigDecimal(parts[13].trim()));
            adjustmentData.setNovAmt(new BigDecimal(parts[14].trim()));
            adjustmentData.setDecAmt(new BigDecimal(parts[15].trim()));
            
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid number format: " + e.getMessage());
        }
        
        return adjustmentData;
    }
    
    private ValidationResult validateAdjustmentData(AdjustmentData adjustmentData) {
        ValidationResult result = new ValidationResult();
        
        if (!validateScenario(adjustmentData.getScenarioId())) {
            result.addError("Invalid scenario ID: " + adjustmentData.getScenarioId());
        }
        
        if (!validateGoc(adjustmentData.getGoc(), adjustmentData.getFiscalYear().toString())) {
            result.addError("Invalid GOC: " + adjustmentData.getGoc());
        }
        
        if (!validateAccount(adjustmentData.getAccount(), adjustmentData.getFiscalYear().toString())) {
            result.addError("Invalid account: " + adjustmentData.getAccount());
        }
        
        if (!validateLeafAccount(adjustmentData.getAccount(), adjustmentData.getFiscalYear().toString())) {
            result.addError("Account is not a leaf account: " + adjustmentData.getAccount());
        }
        
        if (!validateCurrency(adjustmentData.getCurrency())) {
            result.addError("Invalid currency: " + adjustmentData.getCurrency());
        }
        
        return result;
    }
    
    private boolean validateSubmitterRole(Long userId) {
        String sql = "SELECT role FROM users WHERE id = ?";
        try {
            String role = jdbcTemplate.queryForObject(sql, String.class, userId);
            return "submitter".equalsIgnoreCase(role) || "admin".equalsIgnoreCase(role);
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean validateApproverRole(Long userId) {
        String sql = "SELECT role FROM users WHERE id = ?";
        try {
            String role = jdbcTemplate.queryForObject(sql, String.class, userId);
            return "approver".equalsIgnoreCase(role) || "admin".equalsIgnoreCase(role);
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean validateScenario(Long scenarioId) {
        String sql = "SELECT COUNT(*) FROM scenario_info WHERE scenario_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, scenarioId);
        return count != null && count > 0;
    }
    
    private boolean validateGoc(String goc, String periodId) {
        String sql = "SELECT COUNT(*) FROM goc_info WHERE goc = ? AND period_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, goc, periodId);
        return count != null && count > 0;
    }
    
    private boolean validateAccount(String account, String periodId) {
        String sql = "SELECT COUNT(*) FROM account_info WHERE account_id = ? AND period_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, account, periodId);
        return count != null && count > 0;
    }
    
    private boolean validateLeafAccount(String account, String periodId) {
        String sql = "SELECT COUNT(*) FROM account_info WHERE account_parent_id = ? AND period_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, account, periodId);
        return count != null && count == 0;
    }
    
    private boolean validateCurrency(String currency) {
        return VALID_CURRENCIES.contains(currency.toUpperCase());
    }
    
    private void copyAdjustmentDataToFeedData(Long adjustmentSessionId) {
        String sql = "INSERT INTO feed_data (scenario_id, goc, account, fiscal_year, currency, " +
                    "jan_amt, feb_amt, mar_amt, apr_amt, may_amt, jun_amt, jul_amt, aug_amt, " +
                    "sep_amt, oct_amt, nov_amt, dec_amt, created_at) " +
                    "SELECT scenario_id, goc, account, fiscal_year, currency, " +
                    "jan_amt, feb_amt, mar_amt, apr_amt, may_amt, jun_amt, jul_amt, aug_amt, " +
                    "sep_amt, oct_amt, nov_amt, dec_amt, created_at " +
                    "FROM adjustment_data WHERE adjustment_session_id = ?";
        
        jdbcTemplate.update(sql, adjustmentSessionId);
    }
}
