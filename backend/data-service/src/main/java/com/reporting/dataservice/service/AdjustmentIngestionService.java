package com.reporting.dataservice.service;

import com.reporting.dataservice.dto.AdjustmentUploadRequest;
import com.reporting.dataservice.dto.AdjustmentApprovalRequest;
import com.reporting.dataservice.dto.IngestionStatus;
import com.reporting.dataservice.dto.ValidationResult;
import com.reporting.dataservice.exception.ValidationException;
import com.reporting.dataservice.model.AdjustmentData;
import com.reporting.dataservice.model.AdjustmentSession;
import com.reporting.dataservice.model.UserInfo;
import com.reporting.dataservice.repository.AdjustmentDataRepository;
import com.reporting.dataservice.repository.AdjustmentSessionRepository;
import com.reporting.dataservice.repository.UserInfoRepository;
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
import java.util.Map;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Service
public class AdjustmentIngestionService {
    
    @Autowired
    private AdjustmentDataRepository adjustmentDataRepository;
    
    @Autowired
    private UserInfoRepository userInfoRepository;
    
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
        
        try {
            String filename = file.getOriginalFilename();
            if (filename != null && filename.toLowerCase().endsWith(".xlsx")) {
                processExcelFile(file, request.getScenarioId(), session.getSessionId(), adjustmentDataList, errors);
            } else {
                processTextFile(file, request.getScenarioId(), session.getSessionId(), adjustmentDataList, errors);
            }
            
            session.setTotalRecords(adjustmentDataList.size() + errors.size());
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
            
        } catch (Exception e) {
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
        String sql = "SELECT role FROM user_info WHERE user_id = ?";
        try {
            String role = jdbcTemplate.queryForObject(sql, String.class, userId);
            return "submitter".equalsIgnoreCase(role) || "admin".equalsIgnoreCase(role);
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean validateApproverRole(Long userId) {
        String sql = "SELECT role FROM user_info WHERE user_id = ?";
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
    
    private void processExcelFile(MultipartFile file, Long scenarioId, Long sessionId, 
                                 List<AdjustmentData> adjustmentDataList, List<String> errors) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowNumber = 0;
            
            for (Row row : sheet) {
                rowNumber++;
                if (rowNumber == 1) continue; // Skip header row
                
                try {
                    AdjustmentData adjustmentData = parseExcelRow(row, rowNumber, scenarioId);
                    adjustmentData.setAdjustmentSessionId(sessionId);
                    ValidationResult validation = validateAdjustmentData(adjustmentData);
                    
                    if (validation.isValid()) {
                        applyCurrencyTransformation(adjustmentData);
                        adjustmentDataList.add(adjustmentData);
                    } else {
                        final int currentRowNumber = rowNumber;
                        errors.addAll(validation.getErrors().stream()
                            .map(error -> "Row " + currentRowNumber + ": " + error)
                            .toList());
                    }
                } catch (Exception e) {
                    final int currentRowNumber = rowNumber;
                    errors.add("Row " + currentRowNumber + ": " + e.getMessage());
                }
            }
        }
    }
    
    private void processTextFile(MultipartFile file, Long scenarioId, Long sessionId,
                                List<AdjustmentData> adjustmentDataList, List<String> errors) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                try {
                    AdjustmentData adjustmentData = parseAdjustmentLine(line, lineNumber, scenarioId);
                    adjustmentData.setAdjustmentSessionId(sessionId);
                    ValidationResult validation = validateAdjustmentData(adjustmentData);
                    
                    if (validation.isValid()) {
                        applyCurrencyTransformation(adjustmentData);
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
        }
    }
    
    private AdjustmentData parseExcelRow(Row row, int rowNumber, Long scenarioId) {
        AdjustmentData adjustmentData = new AdjustmentData();
        
        try {
            adjustmentData.setScenarioId(scenarioId);
            adjustmentData.setGoc(getCellValueAsString(row.getCell(0)));
            adjustmentData.setAccount(getCellValueAsString(row.getCell(1)));
            adjustmentData.setCurrency(getCellValueAsString(row.getCell(2)));
            adjustmentData.setFiscalYear((int) getCellValueAsDouble(row.getCell(3)));
            
            adjustmentData.setJanAmt(new BigDecimal(getCellValueAsDouble(row.getCell(4))));
            adjustmentData.setFebAmt(new BigDecimal(getCellValueAsDouble(row.getCell(5))));
            adjustmentData.setMarAmt(new BigDecimal(getCellValueAsDouble(row.getCell(6))));
            adjustmentData.setAprAmt(new BigDecimal(getCellValueAsDouble(row.getCell(7))));
            adjustmentData.setMayAmt(new BigDecimal(getCellValueAsDouble(row.getCell(8))));
            adjustmentData.setJunAmt(new BigDecimal(getCellValueAsDouble(row.getCell(9))));
            adjustmentData.setJulAmt(new BigDecimal(getCellValueAsDouble(row.getCell(10))));
            adjustmentData.setAugAmt(new BigDecimal(getCellValueAsDouble(row.getCell(11))));
            adjustmentData.setSepAmt(new BigDecimal(getCellValueAsDouble(row.getCell(12))));
            adjustmentData.setOctAmt(new BigDecimal(getCellValueAsDouble(row.getCell(13))));
            adjustmentData.setNovAmt(new BigDecimal(getCellValueAsDouble(row.getCell(14))));
            adjustmentData.setDecAmt(new BigDecimal(getCellValueAsDouble(row.getCell(15))));
            
        } catch (Exception e) {
            throw new ValidationException("Invalid data format: " + e.getMessage());
        }
        
        return adjustmentData;
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
    
    private double getCellValueAsDouble(Cell cell) {
        if (cell == null) return 0.0;
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                try {
                    return Double.parseDouble(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            default:
                return 0.0;
        }
    }
    
    private void applyCurrencyTransformation(AdjustmentData adjustmentData) {
        String scenarioSql = "SELECT fx_rate FROM scenario_info WHERE scenario_id = ?";
        String fxRate;
        
        try {
            fxRate = jdbcTemplate.queryForObject(scenarioSql, String.class, adjustmentData.getScenarioId());
            if (fxRate == null) {
                throw new ValidationException("No FX rate found for scenario ID: " + adjustmentData.getScenarioId());
            }
        } catch (Exception e) {
            throw new ValidationException("Failed to get FX rate from scenario: " + e.getMessage());
        }
        
        String ratesSql = "SELECT m1_rate, m2_rate, m3_rate, m4_rate, m5_rate, m6_rate, " +
                         "m7_rate, m8_rate, m9_rate, m10_rate, m11_rate, m12_rate " +
                         "FROM fxrate_info WHERE fx_name = ? AND year = ? AND currency = ?";
        
        try {
            Map<String, Object> rates = jdbcTemplate.queryForMap(ratesSql, fxRate, adjustmentData.getFiscalYear(), adjustmentData.getCurrency());
            
            if (adjustmentData.getJanAmt() != null && rates.get("m1_rate") != null) {
                adjustmentData.setJanAmt(adjustmentData.getJanAmt().multiply(new BigDecimal(rates.get("m1_rate").toString())));
            }
            if (adjustmentData.getFebAmt() != null && rates.get("m2_rate") != null) {
                adjustmentData.setFebAmt(adjustmentData.getFebAmt().multiply(new BigDecimal(rates.get("m2_rate").toString())));
            }
            if (adjustmentData.getMarAmt() != null && rates.get("m3_rate") != null) {
                adjustmentData.setMarAmt(adjustmentData.getMarAmt().multiply(new BigDecimal(rates.get("m3_rate").toString())));
            }
            if (adjustmentData.getAprAmt() != null && rates.get("m4_rate") != null) {
                adjustmentData.setAprAmt(adjustmentData.getAprAmt().multiply(new BigDecimal(rates.get("m4_rate").toString())));
            }
            if (adjustmentData.getMayAmt() != null && rates.get("m5_rate") != null) {
                adjustmentData.setMayAmt(adjustmentData.getMayAmt().multiply(new BigDecimal(rates.get("m5_rate").toString())));
            }
            if (adjustmentData.getJunAmt() != null && rates.get("m6_rate") != null) {
                adjustmentData.setJunAmt(adjustmentData.getJunAmt().multiply(new BigDecimal(rates.get("m6_rate").toString())));
            }
            if (adjustmentData.getJulAmt() != null && rates.get("m7_rate") != null) {
                adjustmentData.setJulAmt(adjustmentData.getJulAmt().multiply(new BigDecimal(rates.get("m7_rate").toString())));
            }
            if (adjustmentData.getAugAmt() != null && rates.get("m8_rate") != null) {
                adjustmentData.setAugAmt(adjustmentData.getAugAmt().multiply(new BigDecimal(rates.get("m8_rate").toString())));
            }
            if (adjustmentData.getSepAmt() != null && rates.get("m9_rate") != null) {
                adjustmentData.setSepAmt(adjustmentData.getSepAmt().multiply(new BigDecimal(rates.get("m9_rate").toString())));
            }
            if (adjustmentData.getOctAmt() != null && rates.get("m10_rate") != null) {
                adjustmentData.setOctAmt(adjustmentData.getOctAmt().multiply(new BigDecimal(rates.get("m10_rate").toString())));
            }
            if (adjustmentData.getNovAmt() != null && rates.get("m11_rate") != null) {
                adjustmentData.setNovAmt(adjustmentData.getNovAmt().multiply(new BigDecimal(rates.get("m11_rate").toString())));
            }
            if (adjustmentData.getDecAmt() != null && rates.get("m12_rate") != null) {
                adjustmentData.setDecAmt(adjustmentData.getDecAmt().multiply(new BigDecimal(rates.get("m12_rate").toString())));
            }
            
        } catch (Exception e) {
            throw new ValidationException("Failed to apply currency transformation: " + e.getMessage());
        }
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
    
    public List<UserInfo> getAllUsers() {
        String sql = "SELECT user_id, name, role FROM user_info ORDER BY name";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            UserInfo user = new UserInfo();
            user.setUserId(rs.getLong("user_id"));
            user.setName(rs.getString("name"));
            user.setRole(rs.getString("role"));
            return user;
        });
    }
    
    public List<UserInfo> getUsersByRole(String role) {
        String sql = "SELECT user_id, name, role FROM user_info WHERE role = ? OR role = 'admin' ORDER BY name";
        return jdbcTemplate.query(sql, new Object[]{role}, (rs, rowNum) -> {
            UserInfo user = new UserInfo();
            user.setUserId(rs.getLong("user_id"));
            user.setName(rs.getString("name"));
            user.setRole(rs.getString("role"));
            return user;
        });
    }
}
