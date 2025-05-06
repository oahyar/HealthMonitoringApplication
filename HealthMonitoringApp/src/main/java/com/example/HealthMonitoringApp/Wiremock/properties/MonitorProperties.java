package com.example.HealthMonitoringApp.Wiremock.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "monitor")
public class MonitorProperties {
    /** How often (ms) to run all checks */
    private long rateMillis;
    private long warnThreshold;     // new
    private long critThreshold;     // new

    /** A map of [logicalName → URL] for each endpoint to ping */
    private Map<String,String> endpoints = new HashMap<>();

    // ——— Getters & Setters ———
    public long getRateMillis() {
        return rateMillis;
    }
    public void setRateMillis(long rateMillis) {
        this.rateMillis = rateMillis;
    }
    public Map<String,String> getEndpoints() {
        return endpoints;
    }
    public void setEndpoints(Map<String,String> endpoints) {
        this.endpoints = endpoints;
    }

    public long getWarnThreshold() {
        return warnThreshold;
    }

    public void setWarnThreshold(long warnThreshold) {
        this.warnThreshold = warnThreshold;
    }

    public long getCritThreshold() {
        return critThreshold;
    }

    public void setCritThreshold(long critThreshold) {
        this.critThreshold = critThreshold;
    }
}