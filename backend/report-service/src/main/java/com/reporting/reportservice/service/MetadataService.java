package com.reporting.reportservice.service;

import com.reporting.reportservice.model.TableMetadata;
import com.reporting.reportservice.model.ColumnMetadata;
import com.reporting.reportservice.model.JoinMetadata;
import com.reporting.reportservice.repository.TableMetadataRepository;
import com.reporting.reportservice.repository.ColumnMetadataRepository;
import com.reporting.reportservice.repository.JoinMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MetadataService {
    
    @Autowired
    private TableMetadataRepository tableMetadataRepository;
    
    @Autowired
    private ColumnMetadataRepository columnMetadataRepository;
    
    @Autowired
    private JoinMetadataRepository joinMetadataRepository;
    
    public List<TableMetadata> getAllActiveTables() {
        return tableMetadataRepository.findByIsActiveTrue();
    }
    
    public List<TableMetadata> getAllTablesWithColumns() {
        return tableMetadataRepository.findAllActiveWithColumns();
    }
    
    public TableMetadata getTableByName(String tableName) {
        return tableMetadataRepository.findByTableName(tableName);
    }
    
    public List<ColumnMetadata> getSelectableColumns(String tableName) {
        TableMetadata table = tableMetadataRepository.findByTableName(tableName);
        if (table != null) {
            return columnMetadataRepository.findByTableAndIsSelectableTrue(table);
        }
        return List.of();
    }
    
    public List<ColumnMetadata> getFilterableColumns(String tableName) {
        TableMetadata table = tableMetadataRepository.findByTableName(tableName);
        if (table != null) {
            return columnMetadataRepository.findByTableAndIsFilterableTrue(table);
        }
        return List.of();
    }
    
    public List<JoinMetadata> getAvailableJoins(String tableName) {
        TableMetadata table = tableMetadataRepository.findByTableName(tableName);
        if (table != null) {
            return joinMetadataRepository.findByTable(table);
        }
        return List.of();
    }
    
    @Transactional
    public TableMetadata createTable(TableMetadata tableMetadata) {
        return tableMetadataRepository.save(tableMetadata);
    }
    
    @Transactional
    public ColumnMetadata createColumn(ColumnMetadata columnMetadata) {
        return columnMetadataRepository.save(columnMetadata);
    }
    
    @Transactional
    public JoinMetadata createJoin(JoinMetadata joinMetadata) {
        return joinMetadataRepository.save(joinMetadata);
    }
    
    @Transactional
    public void initializeSampleMetadata() {
        TableMetadata usersTable = new TableMetadata("users", "Users", "User information table");
        TableMetadata ordersTable = new TableMetadata("orders", "Orders", "Customer orders table");
        TableMetadata productsTable = new TableMetadata("products", "Products", "Product catalog table");
        
        usersTable = tableMetadataRepository.save(usersTable);
        ordersTable = tableMetadataRepository.save(ordersTable);
        productsTable = tableMetadataRepository.save(productsTable);
        
        columnMetadataRepository.save(new ColumnMetadata(usersTable, "id", "User ID", "BIGINT"));
        columnMetadataRepository.save(new ColumnMetadata(usersTable, "name", "Full Name", "VARCHAR"));
        columnMetadataRepository.save(new ColumnMetadata(usersTable, "email", "Email Address", "VARCHAR"));
        columnMetadataRepository.save(new ColumnMetadata(usersTable, "created_at", "Registration Date", "TIMESTAMP"));
        
        columnMetadataRepository.save(new ColumnMetadata(ordersTable, "id", "Order ID", "BIGINT"));
        columnMetadataRepository.save(new ColumnMetadata(ordersTable, "user_id", "Customer ID", "BIGINT"));
        columnMetadataRepository.save(new ColumnMetadata(ordersTable, "product_id", "Product ID", "BIGINT"));
        columnMetadataRepository.save(new ColumnMetadata(ordersTable, "quantity", "Quantity", "INTEGER"));
        columnMetadataRepository.save(new ColumnMetadata(ordersTable, "total_amount", "Total Amount", "DECIMAL"));
        columnMetadataRepository.save(new ColumnMetadata(ordersTable, "order_date", "Order Date", "TIMESTAMP"));
        
        columnMetadataRepository.save(new ColumnMetadata(productsTable, "id", "Product ID", "BIGINT"));
        columnMetadataRepository.save(new ColumnMetadata(productsTable, "name", "Product Name", "VARCHAR"));
        columnMetadataRepository.save(new ColumnMetadata(productsTable, "category", "Category", "VARCHAR"));
        columnMetadataRepository.save(new ColumnMetadata(productsTable, "price", "Unit Price", "DECIMAL"));
        
        joinMetadataRepository.save(new JoinMetadata(ordersTable, usersTable, "user_id", "id", "INNER"));
        joinMetadataRepository.save(new JoinMetadata(ordersTable, productsTable, "product_id", "id", "INNER"));
    }
}
