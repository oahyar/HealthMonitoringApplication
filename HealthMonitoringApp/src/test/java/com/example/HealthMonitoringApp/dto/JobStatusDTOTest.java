package com.example.HealthMonitoringApp.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class JobStatusDTOTest {

    @Test
    public void testGettersAndSetters() {
        JobStatusDTO dto = new JobStatusDTO();

        String jobName = "BackupJob";
        String lastStatus = "SUCCESS";
        LocalDateTime lastRun = LocalDateTime.now().minusHours(1);
        LocalDateTime nextRun = LocalDateTime.now().plusHours(1);

        dto.setJobName(jobName);
        dto.setLastStatus(lastStatus);
        dto.setLastRunTime(lastRun);
        dto.setNextRunTime(nextRun);

        assertThat(dto.getJobName()).isEqualTo(jobName);
        assertThat(dto.getLastStatus()).isEqualTo(lastStatus);
        assertThat(dto.getLastRunTime()).isEqualTo(lastRun);
        assertThat(dto.getNextRunTime()).isEqualTo(nextRun);
    }
}
