package com.example.HealthMonitoringApp.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "database_tablespace", schema = "db")
public class TableSpace implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;
    private String hostname;
    private String tablespaceName;
    private String sid;

    @Column(name = "free_space_mb")
    private Long freeSpaceMb;

    @Column(name = "used_space_mb")
    private Long usedSpaceMb;

    @Column(name = "total_space_mb")
    private Long totalSpaceMb;

    @Column(name = "usage_pct")
    private Long usagePct;

    // ✅ REQUIRED: No-Args Constructor for Hibernate
    public TableSpace() {}

    // Constructor with parameters
    public TableSpace(Long id, LocalDateTime timestamp, String hostname, String sid, String tablespaceName,
                      Long freeSpaceMb, Long usedSpaceMb, Long totalSpaceMb, Long usagePct) {
        this.id = id;
        this.timestamp = timestamp;
        this.hostname = hostname;
        this.sid = sid;
        this.tablespaceName = tablespaceName;
        this.freeSpaceMb = freeSpaceMb;
        this.usedSpaceMb = usedSpaceMb;
        this.totalSpaceMb = totalSpaceMb;
        this.usagePct = usagePct;
    }

    // ✅ Getters and Setters (Required for JPA)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }

    public String getTablespaceName() { return tablespaceName; }
    public void setTablespaceName(String tablespaceName) { this.tablespaceName = tablespaceName; }

    public String getSid() { return sid; }
    public void setSid(String sid) { this.sid = sid; }

    public Long getFreeSpaceMb() { return freeSpaceMb; }
    public void setFreeSpaceMb(Long freeSpaceMb) { this.freeSpaceMb = freeSpaceMb; }

    public Long getUsedSpaceMb() { return usedSpaceMb; }
    public void setUsedSpaceMb(Long usedSpaceMb) { this.usedSpaceMb = usedSpaceMb; }

    public Long getTotalSpaceMb() { return totalSpaceMb; }
    public void setTotalSpaceMb(Long totalSpaceMb) { this.totalSpaceMb = totalSpaceMb; }

    public Long getUsagePct() { return usagePct; }
    public void setUsagePct(Long usagePct) { this.usagePct = usagePct; }

}
