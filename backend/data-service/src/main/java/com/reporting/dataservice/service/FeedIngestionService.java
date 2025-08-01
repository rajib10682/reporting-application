package com.reporting.dataservice.service;

import com.reporting.dataservice.dto.IngestionStatus;
import com.reporting.dataservice.dto.ValidationResult;
import com.reporting.dataservice.exception.ValidationException;
import com.reporting.dataservice.model.FeedData;
import com.reporting.dataservice.repository.FeedDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class FeedIngestionService {
    
    @Autowired
    private FeedDataRepository feedDataRepository;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private static final List<String> VALID_CURRENCIES = Arrays.asList("USD", "EUR", "GBP", "JPY", "CAD", "AUD");
    
    public IngestionStatus processFeedFile(MultipartFile file) {
        IngestionStatus status = new IngestionStatus();
        status.setStatus("PROCESSING");
        
        List<FeedData> feedDataList = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                try {
                    FeedData feedData = parseLine(line, lineNumber);
                    ValidationResult validation = validateFeedData(feedData);
                    
                    if (validation.isValid()) {
                        applyCurrencyTransformation(feedData);
                        feedDataList.add(feedData);
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
            
            status.setTotalRecords(lineNumber);
            status.setSuccessfulRecords(feedDataList.size());
            status.setFailedRecords(errors.size());
            
            if (!feedDataList.isEmpty()) {
                feedDataRepository.saveAll(feedDataList);
            }
            
            if (errors.isEmpty()) {
                status.setStatus("COMPLETED");
                status.setMessage("All records processed successfully");
            } else {
                status.setStatus("COMPLETED_WITH_ERRORS");
                status.setMessage("Processed with " + errors.size() + " errors: " + String.join("; ", errors));
            }
            
        } catch (IOException e) {
            status.setStatus("FAILED");
            status.setMessage("Failed to read file: " + e.getMessage());
        }
        
        return status;
    }
    
    private FeedData parseLine(String line, int lineNumber) {
        String[] parts = line.split("~");
        
        if (parts.length != 17) {
            throw new ValidationException("Invalid format. Expected 17 fields, got " + parts.length);
        }
        
        FeedData feedData = new FeedData();
        
        try {
            String scenarioName = parts[0].trim();
            Long scenarioId = getScenarioIdByName(scenarioName);
            if (scenarioId == null) {
                throw new ValidationException("Invalid scenario: " + scenarioName);
            }
            feedData.setScenarioId(scenarioId);
            
            feedData.setGoc(parts[1].trim());
            feedData.setAccount(parts[2].trim());
            feedData.setCurrency(parts[3].trim());
            feedData.setFiscalYear(Integer.parseInt(parts[4].trim()));
            
            feedData.setJanAmt(new BigDecimal(parts[5].trim()));
            feedData.setFebAmt(new BigDecimal(parts[6].trim()));
            feedData.setMarAmt(new BigDecimal(parts[7].trim()));
            feedData.setAprAmt(new BigDecimal(parts[8].trim()));
            feedData.setMayAmt(new BigDecimal(parts[9].trim()));
            feedData.setJunAmt(new BigDecimal(parts[10].trim()));
            feedData.setJulAmt(new BigDecimal(parts[11].trim()));
            feedData.setAugAmt(new BigDecimal(parts[12].trim()));
            feedData.setSepAmt(new BigDecimal(parts[13].trim()));
            feedData.setOctAmt(new BigDecimal(parts[14].trim()));
            feedData.setNovAmt(new BigDecimal(parts[15].trim()));
            feedData.setDecAmt(new BigDecimal(parts[16].trim()));
            
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid number format: " + e.getMessage());
        }
        
        return feedData;
    }
    
    private ValidationResult validateFeedData(FeedData feedData) {
        ValidationResult result = new ValidationResult();
        
        if (!validateScenario(feedData.getScenarioId())) {
            result.addError("Invalid scenario ID: " + feedData.getScenarioId());
        }
        
        if (!validateGoc(feedData.getGoc(), feedData.getFiscalYear().toString())) {
            result.addError("Invalid GOC: " + feedData.getGoc());
        }
        
        if (!validateAccount(feedData.getAccount(), feedData.getFiscalYear().toString())) {
            result.addError("Invalid account: " + feedData.getAccount());
        }
        
        if (!validateLeafAccount(feedData.getAccount(), feedData.getFiscalYear().toString())) {
            result.addError("Account is not a leaf account: " + feedData.getAccount());
        }
        
        if (!validateCurrency(feedData.getCurrency())) {
            result.addError("Invalid currency: " + feedData.getCurrency());
        }
        
        return result;
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
    
    private Long getScenarioIdByName(String scenarioName) {
        String sql = "SELECT scenario_id FROM scenario_info WHERE scenario_name = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Long.class, scenarioName);
        } catch (Exception e) {
            return null;
        }
    }
    
    private void applyCurrencyTransformation(FeedData feedData) {
        String scenarioSql = "SELECT fx_rate FROM scenario_info WHERE scenario_id = ?";
        String fxRate;
        
        try {
            fxRate = jdbcTemplate.queryForObject(scenarioSql, String.class, feedData.getScenarioId());
            if (fxRate == null) {
                throw new ValidationException("No FX rate found for scenario ID: " + feedData.getScenarioId());
            }
        } catch (Exception e) {
            throw new ValidationException("Failed to get FX rate from scenario: " + e.getMessage());
        }
        
        String ratesSql = "SELECT m1_rate, m2_rate, m3_rate, m4_rate, m5_rate, m6_rate, " +
                         "m7_rate, m8_rate, m9_rate, m10_rate, m11_rate, m12_rate " +
                         "FROM fxrate_info WHERE fx_name = ? AND year = ? AND currency = ?";
        
        try {
            Map<String, Object> rates = jdbcTemplate.queryForMap(ratesSql, fxRate, feedData.getFiscalYear(), feedData.getCurrency());
            
            if (feedData.getJanAmt() != null && rates.get("m1_rate") != null) {
                feedData.setJanAmt(feedData.getJanAmt().multiply(new BigDecimal(rates.get("m1_rate").toString())));
            }
            if (feedData.getFebAmt() != null && rates.get("m2_rate") != null) {
                feedData.setFebAmt(feedData.getFebAmt().multiply(new BigDecimal(rates.get("m2_rate").toString())));
            }
            if (feedData.getMarAmt() != null && rates.get("m3_rate") != null) {
                feedData.setMarAmt(feedData.getMarAmt().multiply(new BigDecimal(rates.get("m3_rate").toString())));
            }
            if (feedData.getAprAmt() != null && rates.get("m4_rate") != null) {
                feedData.setAprAmt(feedData.getAprAmt().multiply(new BigDecimal(rates.get("m4_rate").toString())));
            }
            if (feedData.getMayAmt() != null && rates.get("m5_rate") != null) {
                feedData.setMayAmt(feedData.getMayAmt().multiply(new BigDecimal(rates.get("m5_rate").toString())));
            }
            if (feedData.getJunAmt() != null && rates.get("m6_rate") != null) {
                feedData.setJunAmt(feedData.getJunAmt().multiply(new BigDecimal(rates.get("m6_rate").toString())));
            }
            if (feedData.getJulAmt() != null && rates.get("m7_rate") != null) {
                feedData.setJulAmt(feedData.getJulAmt().multiply(new BigDecimal(rates.get("m7_rate").toString())));
            }
            if (feedData.getAugAmt() != null && rates.get("m8_rate") != null) {
                feedData.setAugAmt(feedData.getAugAmt().multiply(new BigDecimal(rates.get("m8_rate").toString())));
            }
            if (feedData.getSepAmt() != null && rates.get("m9_rate") != null) {
                feedData.setSepAmt(feedData.getSepAmt().multiply(new BigDecimal(rates.get("m9_rate").toString())));
            }
            if (feedData.getOctAmt() != null && rates.get("m10_rate") != null) {
                feedData.setOctAmt(feedData.getOctAmt().multiply(new BigDecimal(rates.get("m10_rate").toString())));
            }
            if (feedData.getNovAmt() != null && rates.get("m11_rate") != null) {
                feedData.setNovAmt(feedData.getNovAmt().multiply(new BigDecimal(rates.get("m11_rate").toString())));
            }
            if (feedData.getDecAmt() != null && rates.get("m12_rate") != null) {
                feedData.setDecAmt(feedData.getDecAmt().multiply(new BigDecimal(rates.get("m12_rate").toString())));
            }
            
        } catch (Exception e) {
            throw new ValidationException("Failed to apply currency transformation: " + e.getMessage());
        }
    }
}
