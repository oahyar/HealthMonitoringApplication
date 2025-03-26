package com.example.HealthMonitoringApp.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class AggregatedTableSpaceMetricsTest {

    @Test
    public void testConstructorAndGetters() {
        String hostname = "db-server01";
        String sid = "ORCL01";
        Long totalTablespace = 50000L;
        Long totalAvailableTablespace = 20000L;
        Long totalUsedTablespace = 30000L;
        Long usagePct = 60L;

        AggregatedTableSpaceMetrics metrics = new AggregatedTableSpaceMetrics(
                hostname,
                sid,
                totalTablespace,
                totalAvailableTablespace,
                totalUsedTablespace,
                usagePct
        );

        assertThat(metrics.getHostname()).isEqualTo(hostname);
        assertThat(metrics.getSid()).isEqualTo(sid);
        assertThat(metrics.getTotalTablespace()).isEqualTo(totalTablespace);
        assertThat(metrics.getTotalAvailableTablespace()).isEqualTo(totalAvailableTablespace);
        assertThat(metrics.getTotalUsedTablespace()).isEqualTo(totalUsedTablespace);
        assertThat(metrics.getUsagePct()).isEqualTo(usagePct);
    }

}