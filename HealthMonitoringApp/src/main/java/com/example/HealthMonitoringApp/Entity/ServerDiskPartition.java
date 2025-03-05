package com.example.HealthMonitoringApp.Entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "server_disk_partitions", schema = "diskspace")
public class ServerDiskPartition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String hostname;
    private LocalDateTime timestamp;

    @Column(name = "size_mb")
    private Long sizeMb;

    @Column(name = "available_mb")
    private Long availableMb;

    @Column(name = "used_mb")
    private Long usedMb;

    @Column(name = "usage_pct")
    private Integer usagePct;

    @Column(name = "mounted_on")
    private String mountedOn;

    @Column(name = "filesystem")
    private String filesystem;

    public ServerDiskPartition() {
    }

    // Getters & Setters
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

    public Long getSizeMb() {
        return sizeMb;
    }

    public void setSizeMb(Long sizeMb) {
        this.sizeMb = sizeMb;
    }

    public Long getAvailableMb() {
        return availableMb;
    }

    public void setAvailableMb(Long availableMb) {
        this.availableMb = availableMb;
    }

    public Long getUsedMb() {
        return usedMb;
    }

    public void setUsedMb(Long usedMb) {
        this.usedMb = usedMb;
    }

    public Integer getUsagePct() {
        return usagePct;
    }

    public void setUsagePct(Integer usagePct) {
        this.usagePct = usagePct;
    }

    public String getMountedOn() {
        return mountedOn;
    }

    public void setMountedOn(String mountedOn) {
        this.mountedOn = mountedOn;
    }

    public String getFilesystem() {
        return filesystem;
    }

    public void setFilesystem(String filesystem) {
        this.filesystem = filesystem;
    }
}