package com.example.HealthMonitoringApp.Entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TableSpaceTest {

    @Test
    public void testGettersAndSetters() {
        TableSpace tableSpace = new TableSpace();

        Long id = 1L;
        LocalDateTime timestamp = LocalDateTime.now();
        String hostname = "db-server01";
        String sid = "ORCL01";
        String tablespaceName = "USERS";
        Long freeSpaceMb = 5000L;
        Long usedSpaceMb = 15000L;
        Long totalSpaceMb = 20000L;
        Long usagePct = 75L;

        tableSpace.setId(id);
        tableSpace.setTimestamp(timestamp);
        tableSpace.setHostname(hostname);
        tableSpace.setSid(sid);
        tableSpace.setTablespaceName(tablespaceName);
        tableSpace.setFreeSpaceMb(freeSpaceMb);
        tableSpace.setUsedSpaceMb(usedSpaceMb);
        tableSpace.setTotalSpaceMb(totalSpaceMb);
        tableSpace.setUsagePct(usagePct);

        assertThat(tableSpace.getId()).isEqualTo(id);
        assertThat(tableSpace.getTimestamp()).isEqualTo(timestamp);
        assertThat(tableSpace.getHostname()).isEqualTo(hostname);
        assertThat(tableSpace.getSid()).isEqualTo(sid);
        assertThat(tableSpace.getTablespaceName()).isEqualTo(tablespaceName);
        assertThat(tableSpace.getFreeSpaceMb()).isEqualTo(freeSpaceMb);
        assertThat(tableSpace.getUsedSpaceMb()).isEqualTo(usedSpaceMb);
        assertThat(tableSpace.getTotalSpaceMb()).isEqualTo(totalSpaceMb);
        assertThat(tableSpace.getUsagePct()).isEqualTo(usagePct);
    }

    @Test
    public void testAllArgsConstructor() {
        LocalDateTime timestamp = LocalDateTime.now();

        TableSpace tableSpace = new TableSpace(
                2L,
                timestamp,
                "db-server02",
                "ORCL02",
                "SYSTEM",
                3000L,
                7000L,
                10000L,
                70L
        );

        assertThat(tableSpace.getId()).isEqualTo(2L);
        assertThat(tableSpace.getTimestamp()).isEqualTo(timestamp);
        assertThat(tableSpace.getHostname()).isEqualTo("db-server02");
        assertThat(tableSpace.getSid()).isEqualTo("ORCL02");
        assertThat(tableSpace.getTablespaceName()).isEqualTo("SYSTEM");
        assertThat(tableSpace.getFreeSpaceMb()).isEqualTo(3000L);
        assertThat(tableSpace.getUsedSpaceMb()).isEqualTo(7000L);
        assertThat(tableSpace.getTotalSpaceMb()).isEqualTo(10000L);
        assertThat(tableSpace.getUsagePct()).isEqualTo(70L);
    }

}