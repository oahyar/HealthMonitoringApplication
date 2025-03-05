package com.example.HealthMonitoringApp.Entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "server_metrics", schema = "diskspace") // Ensure correct schema
public class ServerMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incrementing ID
    private Long id;

    private String hostname;
    private LocalDateTime timestamp;
    private int totalDiskspaceMb;
    private int availableDiskspaceMb;
    private int usedDiskspaceMb;
    private int usagePercentageDiskspace;
    private int availableMemoryMb;
    private int cacheMemoryMb;
    private int freeMemoryMb;
    private int totalMemoryMb;
    private int usedMemoryMb;

    public ServerMetric() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getTotalDiskspaceMb() {
        return totalDiskspaceMb;
    }

    public void setTotalDiskspaceMb(int totalDiskspaceMb) {
        this.totalDiskspaceMb = totalDiskspaceMb;
    }

    public int getAvailableDiskspaceMb() {
        return availableDiskspaceMb;
    }

    public void setAvailableDiskspaceMb(int availableDiskspaceMb) {
        this.availableDiskspaceMb = availableDiskspaceMb;
    }

    public int getUsedDiskspaceMb() {
        return usedDiskspaceMb;
    }

    public void setUsedDiskspaceMb(int usedDiskspaceMb) {
        this.usedDiskspaceMb = usedDiskspaceMb;
    }

    public int getUsagePercentageDiskspace() {
        return usagePercentageDiskspace;
    }

    public void setUsagePercentageDiskspace(int usagePercentageDiskspace) {
        this.usagePercentageDiskspace = usagePercentageDiskspace;
    }

    public int getAvailableMemoryMb() {
        return availableMemoryMb;
    }

    public void setAvailableMemoryMb(int availableMemoryMb) {
        this.availableMemoryMb = availableMemoryMb;
    }

    public int getCacheMemoryMb() {
        return cacheMemoryMb;
    }

    public void setCacheMemoryMb(int cacheMemoryMb) {
        this.cacheMemoryMb = cacheMemoryMb;
    }

    public int getFreeMemoryMb() {
        return freeMemoryMb;
    }

    public void setFreeMemoryMb(int freeMemoryMb) {
        this.freeMemoryMb = freeMemoryMb;
    }

    public int getTotalMemoryMb() {
        return totalMemoryMb;
    }

    public void setTotalMemoryMb(int totalMemoryMb) {
        this.totalMemoryMb = totalMemoryMb;
    }

    public int getUsedMemoryMb() {
        return usedMemoryMb;
    }

    public void setUsedMemoryMb(int usedMemoryMb) {
        this.usedMemoryMb = usedMemoryMb;
    }

    @Override
    public String toString() {
        return "ServerMetric{" +
                "id=" + id +
                ", hostname='" + hostname + '\'' +
                ", timestamp=" + timestamp +
                ", totalDiskspaceMb=" + totalDiskspaceMb +
                ", availableDiskspaceMb=" + availableDiskspaceMb +
                ", usedDiskspaceMb=" + usedDiskspaceMb +
                ", usagePercentageDiskspace=" + usagePercentageDiskspace +
                ", availableMemoryMb=" + availableMemoryMb +
                ", cacheMemoryMb=" + cacheMemoryMb +
                ", freeMemoryMb=" + freeMemoryMb +
                ", totalMemoryMb=" + totalMemoryMb +
                ", usedMemoryMb=" + usedMemoryMb +
                '}';
    }
}
