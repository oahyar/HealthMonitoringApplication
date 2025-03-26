package com.example.HealthMonitoringApp.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class AggregatedSpaceMetricsTest {

    @Test
    public void testAllArgsConstructorAndGetters() {
        String hostname = "server01";
        Long totalDiskspace = 100000L;
        Long totalAvailableDisk = 25000L;
        Long totalUsedDisk = 75000L;
        Long usagePct = 75L;

        AggregatedSpaceMetrics metrics = new AggregatedSpaceMetrics(
                hostname,
                totalDiskspace,
                totalAvailableDisk,
                totalUsedDisk,
                usagePct
        );

        assertThat(metrics.getHostname()).isEqualTo(hostname);
        assertThat(metrics.getTotalDiskspace()).isEqualTo(totalDiskspace);
        assertThat(metrics.getTotalAvailableDisk()).isEqualTo(totalAvailableDisk);
        assertThat(metrics.getTotalUsedDisk()).isEqualTo(totalUsedDisk);
        assertThat(metrics.getUsagePct()).isEqualTo(usagePct);
    }

    @Test
    public void testSetters() {
        AggregatedSpaceMetrics metrics = new AggregatedSpaceMetrics(null, null, null, null, null);

        metrics.setHostname("server02");
        metrics.setTotalDiskspace(200000L);
        metrics.setTotalAvailableDisk(50000L);
        metrics.setTotalUsedDisk(150000L);
        metrics.setUsagePct(75L);

        assertThat(metrics.getHostname()).isEqualTo("server02");
        assertThat(metrics.getTotalDiskspace()).isEqualTo(200000L);
        assertThat(metrics.getTotalAvailableDisk()).isEqualTo(50000L);
        assertThat(metrics.getTotalUsedDisk()).isEqualTo(150000L);
        assertThat(metrics.getUsagePct()).isEqualTo(75L);
    }

}