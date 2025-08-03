package com.reporting.dataservice.controller;

import com.reporting.dataservice.dto.AdjustmentUploadRequest;
import com.reporting.dataservice.dto.AdjustmentApprovalRequest;
import com.reporting.dataservice.dto.IngestionStatus;
import com.reporting.dataservice.model.AdjustmentSession;
import com.reporting.dataservice.service.AdjustmentIngestionService;
import com.reporting.dataservice.service.ExcelTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/adjustments")
public class AdjustmentController {
    
    @Autowired
    private AdjustmentIngestionService adjustmentIngestionService;
    
    @Autowired
    private ExcelTemplateService excelTemplateService;
    
    @PostMapping("/upload")
    public ResponseEntity<IngestionStatus> uploadAdjustment(
            @RequestParam("file") MultipartFile file,
            @RequestParam("scenarioId") Long scenarioId,
            @RequestParam("uploadType") String uploadType,
            @RequestParam("submittedBy") Long submittedBy) {
        
        try {
            if (file.isEmpty()) {
                IngestionStatus status = new IngestionStatus("FAILED", "File is empty");
                return ResponseEntity.badRequest().body(status);
            }
            
            String filename = file.getOriginalFilename();
            if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".txt"))) {
                IngestionStatus status = new IngestionStatus("FAILED", "Only .xlsx and .txt files are supported");
                return ResponseEntity.badRequest().body(status);
            }
            
            AdjustmentUploadRequest request = new AdjustmentUploadRequest(scenarioId, uploadType, submittedBy, filename);
            IngestionStatus status = adjustmentIngestionService.processAdjustmentFile(file, request);
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            IngestionStatus status = new IngestionStatus("FAILED", "Error processing file: " + e.getMessage());
            return ResponseEntity.internalServerError().body(status);
        }
    }
    
    @PostMapping("/approve")
    public ResponseEntity<IngestionStatus> processApproval(@RequestBody AdjustmentApprovalRequest request) {
        IngestionStatus status = adjustmentIngestionService.processApproval(request);
        return ResponseEntity.ok(status);
    }
    
    @GetMapping("/pending")
    public ResponseEntity<List<AdjustmentSession>> getPendingAdjustments() {
        List<AdjustmentSession> adjustments = adjustmentIngestionService.getPendingAdjustments();
        return ResponseEntity.ok(adjustments);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AdjustmentSession>> getUserAdjustments(@PathVariable Long userId) {
        List<AdjustmentSession> adjustments = adjustmentIngestionService.getUserAdjustments(userId);
        return ResponseEntity.ok(adjustments);
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<AdjustmentSession>> getAllAdjustments() {
        List<AdjustmentSession> adjustments = adjustmentIngestionService.getAllAdjustments();
        return ResponseEntity.ok(adjustments);
    }
    
    @GetMapping("/template/download")
    public ResponseEntity<ByteArrayResource> downloadTemplate() {
        try {
            byte[] templateData = excelTemplateService.generateAdjustmentTemplate();
            ByteArrayResource resource = new ByteArrayResource(templateData);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=adjustment_template.xlsx");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(templateData.length)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
