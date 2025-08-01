package com.reporting.reportservice.service;

import com.reporting.reportservice.model.ReportRequest;
import com.reporting.reportservice.model.ReportRequest.ReportStatus;
import com.reporting.reportservice.repository.ReportRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class QueueManagerService {
    
    @Autowired
    private ReportRequestRepository reportRequestRepository;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Value("${reporting.concurrency.max-concurrent-reports:5}")
    private int maxConcurrentReports;
    
    @Value("${reporting.concurrency.queue-timeout-minutes:30}")
    private int queueTimeoutMinutes;
    
    private static final String QUEUE_KEY = "report_queue";
    private static final String EXECUTING_KEY = "report_executing";
    
    @Transactional
    public ReportRequest submitReport(ReportRequest reportRequest) {
        reportRequest.setStatus(ReportStatus.PENDING);
        reportRequest = reportRequestRepository.save(reportRequest);
        
        if (canExecuteImmediately(reportRequest.getUserId())) {
            startExecution(reportRequest);
        } else {
            queueReport(reportRequest);
        }
        
        return reportRequest;
    }
    
    private boolean canExecuteImmediately(Long userId) {
        Long executingCount = reportRequestRepository.countByStatus(ReportStatus.EXECUTING);
        
        if (executingCount < maxConcurrentReports) {
            Long userExecutingCount = reportRequestRepository.countByUserIdAndStatus(userId, ReportStatus.EXECUTING);
            return userExecutingCount == 0;
        }
        
        return false;
    }
    
    @Transactional
    public void startExecution(ReportRequest reportRequest) {
        reportRequest.setStatus(ReportStatus.EXECUTING);
        reportRequest.setStartedAt(LocalDateTime.now());
        reportRequestRepository.save(reportRequest);
        
        redisTemplate.opsForSet().add(EXECUTING_KEY, reportRequest.getId());
        redisTemplate.expire(EXECUTING_KEY, queueTimeoutMinutes, TimeUnit.MINUTES);
    }
    
    @Transactional
    public void queueReport(ReportRequest reportRequest) {
        int priority = calculatePriority(reportRequest.getUserId());
        reportRequest.setPriority(priority);
        reportRequest.setStatus(ReportStatus.QUEUED);
        reportRequestRepository.save(reportRequest);
        
        redisTemplate.opsForZSet().add(QUEUE_KEY, reportRequest.getId(), priority);
        redisTemplate.expire(QUEUE_KEY, queueTimeoutMinutes, TimeUnit.MINUTES);
    }
    
    private int calculatePriority(Long userId) {
        Long userExecutingCount = reportRequestRepository.countByUserIdAndStatus(userId, ReportStatus.EXECUTING);
        
        if (userExecutingCount == 0) {
            return 100; // High priority for users without running reports
        } else {
            return 50;  // Lower priority for users with running reports
        }
    }
    
    @Transactional
    public void completeReport(Long reportId, String resultFilePath, String errorMessage) {
        ReportRequest reportRequest = reportRequestRepository.findById(reportId).orElse(null);
        if (reportRequest != null) {
            if (errorMessage != null) {
                reportRequest.setStatus(ReportStatus.FAILED);
                reportRequest.setErrorMessage(errorMessage);
            } else {
                reportRequest.setStatus(ReportStatus.COMPLETED);
                reportRequest.setResultFilePath(resultFilePath);
            }
            reportRequest.setCompletedAt(LocalDateTime.now());
            reportRequestRepository.save(reportRequest);
            
            redisTemplate.opsForSet().remove(EXECUTING_KEY, reportId);
            
            processNextInQueue();
        }
    }
    
    @Transactional
    public void processNextInQueue() {
        Object nextReportId = redisTemplate.opsForZSet().reverseRange(QUEUE_KEY, 0, 0);
        
        if (nextReportId != null && !((java.util.Set<?>) nextReportId).isEmpty()) {
            Long reportId = (Long) ((java.util.Set<?>) nextReportId).iterator().next();
            
            ReportRequest nextReport = reportRequestRepository.findById(reportId).orElse(null);
            if (nextReport != null && nextReport.getStatus() == ReportStatus.QUEUED) {
                if (canExecuteImmediately(nextReport.getUserId())) {
                    redisTemplate.opsForZSet().remove(QUEUE_KEY, reportId);
                    startExecution(nextReport);
                }
            }
        }
    }
    
    public List<ReportRequest> getQueueStatus() {
        return reportRequestRepository.findByStatusInOrderByPriorityDescCreatedAtAsc(
            Arrays.asList(ReportStatus.PENDING, ReportStatus.QUEUED, ReportStatus.EXECUTING)
        );
    }
    
    public List<ReportRequest> getUserReports(Long userId) {
        return reportRequestRepository.findByUserId(userId);
    }
    
    public Long getQueuePosition(Long reportId) {
        Long rank = redisTemplate.opsForZSet().reverseRank(QUEUE_KEY, reportId);
        return rank != null ? rank + 1 : null;
    }
    
    public int getCurrentExecutingCount() {
        Long count = reportRequestRepository.countByStatus(ReportStatus.EXECUTING);
        return count != null ? count.intValue() : 0;
    }
    
    public int getQueueLength() {
        Long count = reportRequestRepository.countByStatus(ReportStatus.QUEUED);
        return count != null ? count.intValue() : 0;
    }
}
