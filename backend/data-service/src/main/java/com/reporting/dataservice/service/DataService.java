package com.reporting.dataservice.service;

import com.reporting.dataservice.model.DataSource;
import com.reporting.dataservice.repository.DataSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DataService {

    @Autowired
    private DataSourceRepository dataSourceRepository;

    public List<DataSource> getAllDataSources() {
        return dataSourceRepository.findAll();
    }

    public DataSource createDataSource(DataSource dataSource) {
        return dataSourceRepository.save(dataSource);
    }

    public Map<String, Object> getAnalyticsData() {
        Map<String, Object> analytics = new HashMap<>();
        
        List<Map<String, Object>> salesData = Arrays.asList(
            Map.of("month", "Jan", "sales", 12000, "profit", 3000),
            Map.of("month", "Feb", "sales", 15000, "profit", 4500),
            Map.of("month", "Mar", "sales", 18000, "profit", 5400),
            Map.of("month", "Apr", "sales", 22000, "profit", 6600),
            Map.of("month", "May", "sales", 25000, "profit", 7500),
            Map.of("month", "Jun", "sales", 28000, "profit", 8400)
        );
        
        List<Map<String, Object>> categoryData = Arrays.asList(
            Map.of("category", "Electronics", "value", 35000),
            Map.of("category", "Clothing", "value", 25000),
            Map.of("category", "Books", "value", 15000),
            Map.of("category", "Home", "value", 20000)
        );
        
        analytics.put("salesTrend", salesData);
        analytics.put("categoryBreakdown", categoryData);
        analytics.put("totalRevenue", 95000);
        analytics.put("totalOrders", 1250);
        
        return analytics;
    }

    public String exportData(String format) {
        switch (format.toLowerCase()) {
            case "csv":
                return "Month,Sales,Profit\nJan,12000,3000\nFeb,15000,4500\nMar,18000,5400";
            case "json":
                return "{\"data\":[{\"month\":\"Jan\",\"sales\":12000,\"profit\":3000}]}";
            default:
                return "Unsupported format";
        }
    }
}
