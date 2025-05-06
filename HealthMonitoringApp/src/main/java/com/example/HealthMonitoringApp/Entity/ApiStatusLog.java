package com.example.HealthMonitoringApp.Entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "api_status_log", schema = "api")
public class ApiStatusLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String apiName;
    private Instant timestamp;
    private boolean up;
    private long latencyMillis;
    @Column(columnDefinition = "text")
    private String message;


    // ————— Getters & Setters —————

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getApiName() {
        return apiName;
    }
    public void setApiName(String apiName) {
        this.apiName = apiName;
    }
    public Instant getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    public boolean isUp() {
        return up;
    }
    public void setUp(boolean up) {
        this.up = up;
    }
    public long getLatencyMillis() {
        return latencyMillis;
    }
    public void setLatencyMillis(long latencyMillis) {
        this.latencyMillis = latencyMillis;
    }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}