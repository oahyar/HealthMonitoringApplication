package com.example.HealthMonitoringApp.Service;

import com.example.HealthMonitoringApp.Entity.ServerDiskPartition;
import com.example.HealthMonitoringApp.Entity.TableSpace;
import com.example.HealthMonitoringApp.Repository.ServerMetricRepository;
import com.example.HealthMonitoringApp.Repository.TableSpaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DataGenerationServiceTest {

    @Mock
    private ServerMetricRepository serverMetricRepository;

    @Mock
    private TableSpaceRepository tableSpaceRepository;

    @InjectMocks
    private DataGenerationService dataGenerationService;

    @Captor
    private ArgumentCaptor<ServerDiskPartition> diskCaptor;

    @Captor
    private ArgumentCaptor<TableSpace> tablespaceCaptor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testUpdateMockDiskUsage_withData() {
        ServerDiskPartition disk = new ServerDiskPartition();
        disk.setId(1L);
        disk.setHostname("server01");
        disk.setSizeMb(1000L);
        disk.setUsedMb(600L);
        disk.setAvailableMb(400L);
        disk.setUsagePct(60);
        disk.setTimestamp(LocalDateTime.now());

        when(serverMetricRepository.findAll()).thenReturn(List.of(disk));

        dataGenerationService.updateMockDiskUsage();

        verify(serverMetricRepository, times(1)).findAll();
        verify(serverMetricRepository, times(1)).save(diskCaptor.capture());

        ServerDiskPartition updatedDisk = diskCaptor.getValue();
        assertThat(updatedDisk.getUsedMb()).isBetween(570L, 630L); // ±5% of 600
        assertThat(updatedDisk.getAvailableMb()).isEqualTo(1000L - updatedDisk.getUsedMb());
        assertThat(updatedDisk.getUsagePct()).isEqualTo((int) ((updatedDisk.getUsedMb() * 100) / 1000));
        assertThat(updatedDisk.getTimestamp()).isNotNull();
    }

    @Test
    public void testUpdateMockDiskUsage_noDisksFound() {
        when(serverMetricRepository.findAll()).thenReturn(List.of());

        dataGenerationService.updateMockDiskUsage();

        verify(serverMetricRepository, times(1)).findAll();
        verify(serverMetricRepository, never()).save(any());
    }

    @Test
    public void testUpdateMockDiskUsage_whenSaveThrowsException_shouldHandleGracefully() {
        ServerDiskPartition disk = new ServerDiskPartition();
        disk.setId(1L);
        disk.setHostname("server01");
        disk.setSizeMb(1000L);
        disk.setUsedMb(600L);
        disk.setAvailableMb(400L);
        disk.setUsagePct(60);
        disk.setTimestamp(LocalDateTime.now());

        when(serverMetricRepository.findAll()).thenReturn(List.of(disk));
        doThrow(new RuntimeException("DB Save failed")).when(serverMetricRepository).save(any(ServerDiskPartition.class));

        // Call the method
        dataGenerationService.updateMockDiskUsage();

        // Verify the save method was called and the exception was handled
        verify(serverMetricRepository, times(1)).save(disk);

    }

    @Test
    public void testUpdateMockTableSpaceUsage_withData() {
        TableSpace tablespace = new TableSpace();
        tablespace.setId(1L);
        tablespace.setHostname("db-server01");
        tablespace.setSid("ORCL01");
        tablespace.setUsedSpaceMb(6000L);
        tablespace.setFreeSpaceMb(4000L);
        tablespace.setTotalSpaceMb(10000L);
        tablespace.setUsagePct(60L);

        when(tableSpaceRepository.findAll()).thenReturn(List.of(tablespace));

        dataGenerationService.updateMockTableSpaceUsage();

        verify(tableSpaceRepository, times(1)).findAll();
        verify(tableSpaceRepository, times(1)).save(tablespaceCaptor.capture());

        TableSpace updated = tablespaceCaptor.getValue();
        assertThat(updated.getUsedSpaceMb()).isBetween(5700L, 6300L); // ±5% of 6000
        assertThat(updated.getFreeSpaceMb()).isEqualTo(10000L - updated.getUsedSpaceMb());
        assertThat(updated.getTotalSpaceMb()).isEqualTo(10000L);
        assertThat(updated.getUsagePct()).isEqualTo((updated.getUsedSpaceMb() * 100) / 10000L);
    }

    @Test
    public void testUpdateMockTableSpaceUsage_noData() {
        when(tableSpaceRepository.findAll()).thenReturn(List.of());

        dataGenerationService.updateMockTableSpaceUsage();

        verify(tableSpaceRepository, times(1)).findAll();
        verify(tableSpaceRepository, never()).save(any());
    }

    @Test
    public void testUpdateMockTableSpaceUsage_whenSaveThrowsException_shouldHandleGracefully() {
        TableSpace tablespace = new TableSpace();
        tablespace.setId(1L);
        tablespace.setHostname("db-server01");
        tablespace.setSid("ORCL01");
        tablespace.setUsedSpaceMb(6000L);
        tablespace.setFreeSpaceMb(4000L);
        tablespace.setTotalSpaceMb(10000L);
        tablespace.setUsagePct(60L);

        when(tableSpaceRepository.findAll()).thenReturn(List.of(tablespace));
        doThrow(new RuntimeException("DB Save failed")).when(tableSpaceRepository).save(any(TableSpace.class));

        // Call the method
        dataGenerationService.updateMockTableSpaceUsage();

        // Verify repository was called and exception was caught
        verify(tableSpaceRepository, times(1)).save(tablespace);
    }

}