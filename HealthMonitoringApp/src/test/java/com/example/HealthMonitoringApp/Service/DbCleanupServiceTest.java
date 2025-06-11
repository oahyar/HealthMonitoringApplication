package com.example.HealthMonitoringApp.Service;

import com.example.HealthMonitoringApp.Repository.ApiStatusLogRepository;
import com.example.HealthMonitoringApp.Repository.JobLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DbCleanupServiceTest {
    private ApiStatusLogRepository apiRepo;
    private JobLogRepository jobRepo;
    private DbCleanupService cleanupService;

    @BeforeEach
    void setUp() {
        apiRepo = mock(ApiStatusLogRepository.class);
        jobRepo = mock(JobLogRepository.class);
        cleanupService = new DbCleanupService(apiRepo, jobRepo);
    }

    @Test
    void testPurgeOldLogs_callsDeleteMethods() {
        // Arrange: return dummy values for deletes
        when(apiRepo.deleteByTimestampBefore(any(Instant.class))).thenReturn(5);
        when(jobRepo.deleteByStartTimeBefore(any(LocalDateTime.class))).thenReturn(3);

        // Act
        cleanupService.purgeOldLogs();

        // Assert
        verify(apiRepo, times(1)).deleteByTimestampBefore(any(Instant.class));
        verify(jobRepo, times(1)).deleteByStartTimeBefore(any(LocalDateTime.class));
    }
}

