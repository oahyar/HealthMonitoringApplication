package com.example.HealthMonitoringApp.dto;

import jakarta.persistence.criteria.CriteriaBuilder;

public class AggregatedSpaceMetrics {
    private String hostname;
    private Long totalDiskspace;
    private Long totalAvailableDisk;
    private Long totalUsedDisk;
    private Long usagePct;

    public AggregatedSpaceMetrics(String hostname, Long totalDiskspace, Long totalAvailableDisk, Long totalUsedDisk, Long usagePct) {
        this.hostname = hostname;
        this.totalDiskspace = totalDiskspace;
        this.totalAvailableDisk = totalAvailableDisk;
        this.totalUsedDisk = totalUsedDisk;
        this.usagePct = usagePct;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Long getTotalDiskspace() {
        return totalDiskspace;
    }

    public void setTotalDiskspace(Long totalDiskspace) {
        this.totalDiskspace = totalDiskspace;
    }

    public Long getTotalAvailableDisk() {
        return totalAvailableDisk;
    }

    public void setTotalAvailableDisk(Long totalAvailableDisk) {
        this.totalAvailableDisk = totalAvailableDisk;
    }

    public Long getTotalUsedDisk() {
        return totalUsedDisk;
    }

    public void setTotalUsedDisk(Long totalUsedDisk) {
        this.totalUsedDisk = totalUsedDisk;
    }

    public Long getUsagePct() {
        return usagePct;
    }

    public void setUsagePct(Long usagePct) {
        this.usagePct = usagePct;
    }
}
