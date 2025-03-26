package com.example.HealthMonitoringApp.Entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ServerDiskPartitionTest {

    @Test
    public void testGettersAndSetters() {
        ServerDiskPartition partition = new ServerDiskPartition();

        Long id = 1L;
        String hostname = "server01";
        LocalDateTime timestamp = LocalDateTime.now();
        Long sizeMb = 100000L;
        Long availableMb = 30000L;
        Long usedMb = 70000L;
        Integer usagePct = 70;
        String mountedOn = "/";
        String filesystem = "/dev/sda1";

        partition.setId(id);
        partition.setHostname(hostname);
        partition.setTimestamp(timestamp);
        partition.setSizeMb(sizeMb);
        partition.setAvailableMb(availableMb);
        partition.setUsedMb(usedMb);
        partition.setUsagePct(usagePct);
        partition.setMountedOn(mountedOn);
        partition.setFilesystem(filesystem);

        assertThat(partition.getId()).isEqualTo(id);
        assertThat(partition.getHostname()).isEqualTo(hostname);
        assertThat(partition.getTimestamp()).isEqualTo(timestamp);
        assertThat(partition.getSizeMb()).isEqualTo(sizeMb);
        assertThat(partition.getAvailableMb()).isEqualTo(availableMb);
        assertThat(partition.getUsedMb()).isEqualTo(usedMb);
        assertThat(partition.getUsagePct()).isEqualTo(usagePct);
        assertThat(partition.getMountedOn()).isEqualTo(mountedOn);
        assertThat(partition.getFilesystem()).isEqualTo(filesystem);
    }
}