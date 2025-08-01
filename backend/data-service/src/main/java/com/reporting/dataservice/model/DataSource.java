package com.reporting.dataservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "data_sources")
public class DataSource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(nullable = false)
    private String type;
    
    @Column(name = "connection_string")
    private String connectionString;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public DataSource() {
        this.createdAt = LocalDateTime.now();
    }

    public DataSource(String name, String description, String type, String connectionString) {
        this();
        this.name = name;
        this.description = description;
        this.type = type;
        this.connectionString = connectionString;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getConnectionString() { return connectionString; }
    public void setConnectionString(String connectionString) { this.connectionString = connectionString; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
