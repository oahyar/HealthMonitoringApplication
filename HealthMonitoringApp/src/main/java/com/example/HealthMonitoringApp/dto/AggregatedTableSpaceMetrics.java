package com.example.HealthMonitoringApp.dto;

public class AggregatedTableSpaceMetrics {
    private String hostname;
    private String sid;
    private Long totalTablespace;
    private Long totalAvailableTablespace;
    private Long totalUsedTablespace;
    private Long usagePct;

    // ✅ Corrected Constructor (Removed `id` and `timestamp`)
    public AggregatedTableSpaceMetrics(String hostname, String sid, Long totalTablespace, Long totalAvailableTablespace, Long totalUsedTablespace, Long usagePct) {
        this.hostname = hostname;
        this.sid = sid;
        this.totalTablespace = totalTablespace;
        this.totalAvailableTablespace = totalAvailableTablespace;
        this.totalUsedTablespace = totalUsedTablespace;
        this.usagePct = usagePct;
    }

    // ✅ Getters (Spring Boot needs these for JSON serialization)
    public String getHostname() {
        return hostname;
    }

    public String getSid() {
        return sid;
    }

    public Long getTotalTablespace() {
        return totalTablespace;
    }

    public Long getTotalAvailableTablespace() {
        return totalAvailableTablespace;
    }

    public Long getTotalUsedTablespace() {
        return totalUsedTablespace;
    }

    public Long getUsagePct() {
        return usagePct;
    }
}
