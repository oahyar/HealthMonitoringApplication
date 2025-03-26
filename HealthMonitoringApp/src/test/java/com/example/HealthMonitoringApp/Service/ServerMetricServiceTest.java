package com.example.HealthMonitoringApp.Service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.HealthMonitoringApp.Entity.ServerDiskPartition;
import com.example.HealthMonitoringApp.Repository.ServerMetricRepository;
import com.example.HealthMonitoringApp.dto.AggregatedSpaceMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

class ServerMetricServiceTest {

    @Mock
    private ServerMetricRepository serverMetricRepository;

    @InjectMocks
    private ServerMetricService serverMetricService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAggregatedSpaceMetrics_successfulConversion() {
        Object[] row = new Object[] {"server01", 100000L, 40000L, 60000L, 60L};
        when(serverMetricRepository.findAggregatedSpaceMetrics()).thenReturn(List.<Object[]>of(row));

        List<AggregatedSpaceMetrics> result = serverMetricService.getAggregatedSpaceMetrics();

        assertThat(result).hasSize(1);
        AggregatedSpaceMetrics metrics = result.get(0);
        assertThat(metrics.getHostname()).isEqualTo("server01");
        assertThat(metrics.getTotalDiskspace()).isEqualTo(100000L);
        assertThat(metrics.getTotalAvailableDisk()).isEqualTo(40000L);
        assertThat(metrics.getTotalUsedDisk()).isEqualTo(60000L);
        assertThat(metrics.getUsagePct()).isEqualTo(60L);
    }

    @Test
    public void testGetAggregatedSpaceMetrics_emptyResult_shouldReturnEmptyList() {
        when(serverMetricRepository.findAggregatedSpaceMetrics()).thenReturn(List.of());

        List<AggregatedSpaceMetrics> result = serverMetricService.getAggregatedSpaceMetrics();

        assertThat(result).isEmpty();
    }

    @Test
    public void testGetAggregatedSpaceMetrics_withInvalidRow_shouldSkipAndContinue() {
        // One good row, one bad row (row[0] is null, so toString() will throw NPE)
        Object[] validRow = new Object[]{"server01", 100000L, 40000L, 60000L, 60L};
        Object[] invalidRow = new Object[]{null, "invalid", null, 123, "oops"};

        when(serverMetricRepository.findAggregatedSpaceMetrics())
                .thenReturn(List.of(validRow, invalidRow));

        List<AggregatedSpaceMetrics> result = serverMetricService.getAggregatedSpaceMetrics();

        // Only the valid row should be processed successfully
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getHostname()).isEqualTo("server01");

        verify(serverMetricRepository).findAggregatedSpaceMetrics();
    }

    @Test
    public void testGetDiskDetailByHostname_returnsPartitions() {
        String hostname = "server01";

        ServerDiskPartition partition = new ServerDiskPartition();
        partition.setHostname(hostname);
        partition.setSizeMb(1000L);
        partition.setAvailableMb(500L);
        partition.setUsedMb(500L);
        partition.setMountedOn("/");
        partition.setFilesystem("/dev/sda1");

        when(serverMetricRepository.findLatestFilesystemByHostname(hostname))
                .thenReturn(List.of(partition));

        List<ServerDiskPartition> result = serverMetricService.getDiskDetailByHostname(hostname);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getHostname()).isEqualTo("server01");
        verify(serverMetricRepository, times(1)).findLatestFilesystemByHostname(hostname);
    }

    @Test
    public void testGetDiskDetailByHostname_returnsEmptyList() {
        String hostname = "unknown";

        when(serverMetricRepository.findLatestFilesystemByHostname(hostname))
                .thenReturn(Collections.emptyList());

        List<ServerDiskPartition> result = serverMetricService.getDiskDetailByHostname(hostname);

        assertThat(result).isEmpty();
        verify(serverMetricRepository, times(1)).findLatestFilesystemByHostname(hostname);
    }

    @Test
    public void testGetHighUsageFilesystems_returnsResults() {
        String hostname = "server01";

        ServerDiskPartition partition = new ServerDiskPartition();
        partition.setHostname(hostname);
        partition.setFilesystem("/dev/sda1");
        partition.setMountedOn("/");
        partition.setUsagePct(92);
        partition.setSizeMb(1000L);
        partition.setUsedMb(920L);
        partition.setAvailableMb(80L);

        when(serverMetricRepository.findHighUsageFilesystems(hostname))
                .thenReturn(List.of(partition));

        List<ServerDiskPartition> result = serverMetricService.getHighUsageFilesystems(hostname);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getHostname()).isEqualTo(hostname);
        assertThat(result.get(0).getUsagePct()).isGreaterThan(90);

        verify(serverMetricRepository, times(1)).findHighUsageFilesystems(hostname);
    }

    @Test
    public void testGetHighUsageFilesystems_returnsEmptyList() {
        String hostname = "server01";

        when(serverMetricRepository.findHighUsageFilesystems(hostname))
                .thenReturn(Collections.emptyList());

        List<ServerDiskPartition> result = serverMetricService.getHighUsageFilesystems(hostname);

        assertThat(result).isEmpty();

        verify(serverMetricRepository, times(1)).findHighUsageFilesystems(hostname);
    }

}